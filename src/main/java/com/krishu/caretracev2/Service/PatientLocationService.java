package com.krishu.caretracev2.Service;

import com.krishu.caretracev2.CustomExceptions.NotFoundException;
import com.krishu.caretracev2.CustomExceptions.PatientLocationNotKnowException;
import com.krishu.caretracev2.CustomExceptions.UnauthorizedException;
import com.krishu.caretracev2.DTO.LocationHistoryResponse;
import com.krishu.caretracev2.DTO.PatientLocationRequest;
import com.krishu.caretracev2.DTO.PatientLocationResponse;
import com.krishu.caretracev2.GeoUtils;
import com.krishu.caretracev2.Model.CareTaker;
import com.krishu.caretracev2.Model.LocationHistory;
import com.krishu.caretracev2.Model.Patient;
import com.krishu.caretracev2.Model.PatientLocation;
import com.krishu.caretracev2.Repository.CareTakerRepo;
import com.krishu.caretracev2.Repository.LocationHistoryRepo;
import com.krishu.caretracev2.Repository.PatientLocationRepo;
import com.krishu.caretracev2.Repository.PatientRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PatientLocationService {

    private static final Logger log = LoggerFactory.getLogger(PatientLocationService.class);

    private final PatientLocationRepo patientLocationRepo;
    private final PatientRepo patientRepo;
    private final AlertService alertService;
    private final CareTakerRepo careTakerRepo;
    private final LocationHistoryRepo locationHistoryRepo;

    // History settings. These three have NO default value here, so all three must exist in
    // application.properties or the application will not start.

    // A new history point is only worth saving if the patient moved at least this many metres.
    @Value("${location.history.min.distance.meters}")
    private double minDistanceMeters;
    // Minimum minutes that must pass since the last saved point before another one is saved.
    @Value("${location.history.max.interval.minutes}")
    private long maxIntervalMinutes;
    // Upper limit on how many points one history request may return.
    @Value("${location.history.max.points}")
    private int maxPoints;

    public PatientLocationService(PatientLocationRepo patientLocationRepo, PatientRepo patientRepo,
                                  AlertService alertService,
                                  CareTakerRepo careTakerRepo,LocationHistoryRepo locationHistoryRepo) {
        this.patientLocationRepo = patientLocationRepo;
        this.patientRepo = patientRepo;
        this.alertService = alertService;
        this.careTakerRepo = careTakerRepo;
        this.locationHistoryRepo=locationHistoryRepo;
    }

    /**
     * Called by the patient's phone every time it sends a new GPS position.
     * Order of work: validate -> save current location -> record history (best effort) -> safe-zone check.
     */
    public PatientLocationResponse updateLocation(Authentication authentication, PatientLocationRequest request){
        // Reject missing or impossible coordinates. A null value here would otherwise crash the
        // distance calculation later (Double unboxing) and break the safe-zone check.
        if(request.getLatitude()==null || request.getLatitude()<-90 || request.getLatitude()>90
                || request.getLongitude()==null || request.getLongitude()<-180 || request.getLongitude()>180){
            throw new IllegalArgumentException("A valid latitude and longitude are required");
        }
        // The patient is identified from the login token, never from the request body.
        Patient patient=patientRepo.findByUserId(authentication.getName()).orElseThrow(()->
                new NotFoundException("Patient not found"));
        // Upsert: reuse the patient's single "current location" document, or create it the first time.
        PatientLocation location = patientLocationRepo
                .findByPatientId(patient.getId())
                .orElseGet(PatientLocation::new);

        location.setPatientId(patient.getId());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setUpdatedAt(LocalDateTime.now());
        PatientLocation savedLocation = patientLocationRepo.save(location);
        // History is best effort: if recording fails it is only logged, so a history problem can
        // never block the location update or the safety alert below.
        try {
            recordIfNeeded(patient.getId(), request.getLatitude(), request.getLongitude(), savedLocation.getUpdatedAt());
        } catch (RuntimeException e) {
            log.warn("Could not record location history for patient {}: {}", patient.getId(), e.getMessage());
        }
        // Safety check: creates or resolves the "left all safe zones" alert based on the NEW location.
        // It reads the location we just saved, so it must stay after the save above.
        alertService.checkGeoFenceAlert(patient.getId());
        return mapToResponse(savedLocation);
    }

    // The patient reads their own current location (no patientId needed, it comes from the token).
    public PatientLocationResponse getMyLocation(Authentication authentication) {
        Patient patient=patientRepo.findByUserId(authentication.getName()).orElseThrow(()->new NotFoundException("Patient not found"));
        // Throws PatientLocationNotKnowException if the patient has never sent a location.
        PatientLocation currLocation=patientLocationRepo.findByPatientId(patient.getId()).
                orElseThrow(()->new PatientLocationNotKnowException("Patient not Known"));
        return mapToResponse(currLocation);
    }

    // A caretaker reads their patient's current location. Fails with 403 for someone else's patient.
    public PatientLocationResponse getPatientLocation(String patientId, Authentication authentication) {
        getAuthorizedPatient(patientId,authentication);
        PatientLocation location=patientLocationRepo.findByPatientId(patientId)
                .orElseThrow(()->new PatientLocationNotKnowException("Location not known"));
        return mapToResponse(location);
    }

    /**
     * Caretaker-only. Returns the saved track between "from" and "to", oldest point first.
     * Without from/to it returns the last 24 hours.
     * Note: the database query treats both ends of the range as exclusive.
     */
    public List<LocationHistoryResponse> getLocationHistory(String patientId, LocalDateTime from, LocalDateTime to
            , Authentication authentication){
        getAuthorizedPatient(patientId,authentication);
        LocalDateTime start=(from!=null)?from:LocalDateTime.now().minusHours(24);
        LocalDateTime end=(to!=null)?to:LocalDateTime.now();
        List<LocationHistory> points=locationHistoryRepo.
                findByPatientIdAndRecordedAtBetweenOrderByRecordedAtAsc(patientId,start,end);
        if(points.size()>maxPoints){
            // NOTE (not changed): subList() returns a trimmed VIEW and does not modify "points".
            // Because the result is not assigned to anything, this line currently has no effect and
            // the maxPoints limit is NOT applied. To make it work: points = points.subList(...)
            points.subList(points.size()-maxPoints,points.size()); //keeping the most recent ones
        }
        // Only latitude, longitude and time are returned. The id and patientId are not exposed.
        return points.stream().map(point->{
            LocationHistoryResponse response=new LocationHistoryResponse();
            response.setLatitude(point.getLatitude());
            response.setLongitude(point.getLongitude());
            response.setRecordedAt(point.getRecordedAt());
            return response;
        }).toList();
    }

    // Converts the stored document into the response sent to the app.
    private PatientLocationResponse mapToResponse(PatientLocation savedLocation) {
        PatientLocationResponse response = new PatientLocationResponse();
        response.setId(savedLocation.getId());
        response.setPatientId(savedLocation.getPatientId());
        response.setLatitude(savedLocation.getLatitude());
        response.setLongitude(savedLocation.getLongitude());
        response.setUpdatedAt(savedLocation.getUpdatedAt());
        return response;
    }

    // Access check for caretaker endpoints: the logged-in caretaker must be the one assigned to this patient.
    // Throws NotFoundException (unknown caretaker or patient) or UnauthorizedException (not their patient).
    private void getAuthorizedPatient(String patientId, Authentication authentication) {
        CareTaker careTaker = careTakerRepo.findByUserId(authentication.getName()).orElseThrow(() -> new NotFoundException("CareTaker not found"));
        Patient patient = patientRepo.findById(patientId).orElseThrow(() -> new NotFoundException("Patient not found"));
        if (!patient.getCareTakerId().equals(careTaker.getId())) {
            throw new UnauthorizedException("You are not authorized for this patient");
        }
    }

    /**
     * Decides whether this position should be added to the history.
     * Without this filter a phone sending a position every few seconds would create thousands of
     * near-identical points per day.
     *
     * The very first point for a patient is always saved.
     *
     * NOTE (not changed): the rule as written below is
     *     skip if (moved < minDistanceMeters  OR  not enough time has passed)
     * so a point is saved only when the patient moved far enough AND the interval has also passed.
     * That means a moving patient is recorded at most once per interval, and a patient standing still
     * never gets a "still here" point.
     * The rule I described earlier was
     *     skip only if (moved < minDistanceMeters  AND  not enough time has passed)
     * i.e. save when they moved far enough OR the interval has passed. The difference is
     * "||" versus "&&" in the if-statement below.
     */
    private void recordIfNeeded(String patientId,Double latitude,Double longitude,LocalDateTime time){
        // The most recent point already saved for this patient (if any).
        Optional<LocationHistory> locations=locationHistoryRepo.findFirstByPatientIdOrderByRecordedAtDesc(patientId);
        if(locations.isPresent()){
            LocationHistory prev=locations.get();
            // Distance in metres between the last saved point and the new position.
            double moved= GeoUtils.distanceInMeters(prev.getLatitude(),prev.getLongitude(),latitude,longitude);
            // true when at least maxIntervalMinutes have passed since the last saved point:
            // "the earliest allowed time for the next point is NOT still in the future".
            boolean waitedLongEnough=!prev.getRecordedAt().plusMinutes(maxIntervalMinutes).isAfter(time);
            if(moved<minDistanceMeters || !waitedLongEnough){
                return;   // not worth saving, skip
            }
        }
        // Save the new point.
        LocationHistory point=new LocationHistory();
        point.setPatientId(patientId);
        point.setLatitude(latitude);
        point.setLongitude(longitude);
        point.setRecordedAt(time);
        locationHistoryRepo.save(point);
    }
}

