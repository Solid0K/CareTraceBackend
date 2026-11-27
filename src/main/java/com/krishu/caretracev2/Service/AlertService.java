package com.krishu.caretracev2.Service;

import com.krishu.caretracev2.AlertStatus;
import com.krishu.caretracev2.AlertType;
import com.krishu.caretracev2.CustomExceptions.AlreadyResolvedAlertException;
import com.krishu.caretracev2.CustomExceptions.NotFoundException;
import com.krishu.caretracev2.CustomExceptions.NotRelatedException;
import com.krishu.caretracev2.DTO.AlertResponse;
import com.krishu.caretracev2.DTO.GeoFenceResponse;
import com.krishu.caretracev2.Model.Alert;
import com.krishu.caretracev2.Model.CareTaker;
import com.krishu.caretracev2.Model.Patient;
import com.krishu.caretracev2.NotificationType;
import com.krishu.caretracev2.Repository.AlertRepo;
import com.krishu.caretracev2.Repository.CareTakerRepo;
import com.krishu.caretracev2.Repository.PatientRepo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AlertService {

    private final AlertRepo alertRepo;
    private final GeoFencingService geofenceService;
    private final CareTakerRepo careTakerRepo;
    private final NotificationService notificationService;
    private final PatientRepo patientRepo;

    public AlertService(AlertRepo alertRepo, GeoFencingService geofenceService, CareTakerRepo careTakerRepo,
                        NotificationService notificationService, PatientRepo patientRepo) {
        this.alertRepo = alertRepo;
        this.geofenceService = geofenceService;
        this.careTakerRepo = careTakerRepo;
        this.notificationService = notificationService;
        this.patientRepo = patientRepo;
    }

    public AlertResponse checkGeoFenceAlert(String patientId){
        GeoFenceResponse response=geofenceService.checkPatientGeoFence(patientId);
        Optional<Alert> activeAlert=alertRepo.
                findByPatientIdAndTypeAndStatus(patientId, AlertType.GEOFENCE_BREACH, AlertStatus.ACTIVE);
        boolean safe=response.getStatues().isEmpty() || response.getInsideAnySafeLocation();
        if(safe){
            if(activeAlert.isPresent()){
                Alert alert=activeAlert.get();
                alert.setStatus(AlertStatus.RESOLVED);
                alert.setResolvedAt(LocalDateTime.now());
                Alert resolvedAlert=alertRepo.save(alert);

                patientRepo.findById(patientId).ifPresent(patient ->
                        notificationService.createNotification(patient.getCareTakerId(), patientId,
                                resolvedAlert.getId(),
                        NotificationType.GEOFENCE_RESOLVED, "Patient is back inside a safe location"));
                return mapToAlertResponse(resolvedAlert);
            }
            return null;
        }
        if(activeAlert.isPresent()){
            return null;
        }
        Alert alert=new Alert();
        alert.setPatientId(patientId);
        alert.setMessage("Patient is not any Safe Location");
        alert.setStatus(AlertStatus.ACTIVE);
        alert.setType(AlertType.GEOFENCE_BREACH);
        alert.setCreatedAt(LocalDateTime.now());
        Alert savedAlert=alertRepo.save(alert);
        Patient patient=patientRepo.findById(patientId).orElseThrow(()->new NotFoundException("Patient not found"));
        notificationService.createNotification(patient.getCareTakerId(),patientId,savedAlert.getId(),
                NotificationType.GEOFENCE_BREACH,"Patient is outside of all safe location");
        return mapToAlertResponse(savedAlert);
    }

    public AlertResponse createSoSAlert(Authentication authentication){
        Patient patient=patientRepo.findByUserId(authentication.getName()).orElseThrow(()->
                new NotFoundException("Patient not found"));
        Optional<Alert> activeAlert=alertRepo.
                findByPatientIdAndTypeAndStatus(patient.getId(), AlertType.SOS, AlertStatus.ACTIVE);
        if(activeAlert.isPresent()){
            return mapToAlertResponse(activeAlert.get());
        }
        Alert alert=new Alert();
        alert.setPatientId(patient.getId());
        alert.setMessage("Emergency SOS");
        alert.setStatus(AlertStatus.ACTIVE);
        alert.setType(AlertType.SOS);
        alert.setCreatedAt(LocalDateTime.now());
        Alert savedAlert=alertRepo.save(alert);
        notificationService.createNotification(patient.getCareTakerId(),patient.getId(),savedAlert.getId(),
                NotificationType.SOS,"Emergency SOS");
        return mapToAlertResponse(savedAlert);
    }

    public List<AlertResponse> getPatientsAlerts(Authentication authentication) {
        CareTaker careTaker=careTakerRepo.findByUserId(authentication.getName()).orElseThrow(()->
                new NotFoundException("Caretaker not found"));
        List<String> patientIds=careTaker.getPatientIds();
        List<Alert> alerts=new ArrayList<>();
        for(String patientId:patientIds){
            List<Alert> alert=alertRepo.findByPatientId(patientId);
            alerts.addAll(alert);
        }
        return alerts.stream().map(this::mapToAlertResponse).toList();
    }

    public AlertResponse ResolveSoSAlert(String alertId,Authentication authentication){
        Alert alert=alertRepo.findById(alertId).orElseThrow(()->new NotFoundException("Alert not found"));
        CareTaker careTaker=careTakerRepo.findByUserId(authentication.getName()).orElseThrow(()->
                new NotFoundException("CareTaker not found"));
        Patient patient=patientRepo.findById(alert.getPatientId()).orElseThrow(()->
                new NotFoundException("Patient not found"));
        if(!patient.getCareTakerId().equals(careTaker.getId())){
            throw new NotRelatedException("This alert is not from your patient so you cant resolve it");
        }
        if(alert.getStatus()==AlertStatus.RESOLVED){
            throw new AlreadyResolvedAlertException("Alert is already Resolved");
        }
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        Alert savedAlert=alertRepo.save(alert);
        return mapToAlertResponse(savedAlert);
    }

    private AlertResponse mapToAlertResponse(Alert alert){
        AlertResponse response=new AlertResponse();
        response.setId(alert.getId());
        response.setPatientId(alert.getPatientId());
        response.setMessage(alert.getMessage());
        response.setType(alert.getType());
        response.setStatus(alert.getStatus());
        response.setCreatedAt(alert.getCreatedAt());
        response.setResolvedAt(alert.getResolvedAt());
        return response;
    }
}
