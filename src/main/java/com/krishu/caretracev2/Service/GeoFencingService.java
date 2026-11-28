package com.krishu.caretracev2.Service;

import com.krishu.caretracev2.CustomExceptions.NotFoundException;
import com.krishu.caretracev2.DTO.GeoFenceResponse;
import com.krishu.caretracev2.DTO.SafeLocationStatus;
import com.krishu.caretracev2.GeoUtils;
import com.krishu.caretracev2.LocationEventType;
import com.krishu.caretracev2.Model.LocationEvent;
import com.krishu.caretracev2.Model.PatientLocation;
import com.krishu.caretracev2.Model.SafeZone;
import com.krishu.caretracev2.Repository.LocationEventRepo;
import com.krishu.caretracev2.Repository.PatientLocationRepo;
import com.krishu.caretracev2.Repository.SafeZoneRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GeoFencingService {

    private final PatientLocationRepo patientLocationRepo;
    private final SafeZoneRepo safeLocationRepo;
    private final LocationEventRepo locationEventRepo;

    @Value("${location.exit.buffer.meters}")
    private Double exitBufferMeters;

    public GeoFencingService(PatientLocationRepo patientLocationRepo,
                             SafeZoneRepo safeLocationRepo,LocationEventRepo locationEventRepo) {
        this.patientLocationRepo = patientLocationRepo;
        this.safeLocationRepo = safeLocationRepo;
        this.locationEventRepo=locationEventRepo;
    }

    public GeoFenceResponse checkPatientGeoFence(String patientId){
        PatientLocation patientLocation=patientLocationRepo.findByPatientId(patientId).orElseThrow(()
                ->new NotFoundException("Patient Location not found"));
        List<SafeZone> safeZones=safeLocationRepo.findByPatientId(patientId);

        List<SafeLocationStatus> statues=new ArrayList<>();
        boolean isInsideSafeZone=false;
        for(SafeZone zone:safeZones){
            double distance= GeoUtils.distanceInMeters(patientLocation.getLatitude(), patientLocation.getLongitude()
                    , zone.getLatitude(), zone.getLongitude());
            boolean inside=distance<=zone.getRadius();
            if(inside){
                isInsideSafeZone=true;
            }
            SafeLocationStatus status=new SafeLocationStatus();
            status.setSafeZoneId(zone.getId());
            status.setName(zone.getName());
            status.setDistance(distance);
            status.setInside(inside);
            statues.add(status);
        }
        GeoFenceResponse response=new GeoFenceResponse();
        response.setPatientId(patientId);
        response.setInsideAnySafeLocation(isInsideSafeZone);
        response.setStatues(statues);
        return response;
    }

    public void trackZones(PatientLocation location){
        List<SafeZone> safeZones=safeLocationRepo.findByPatientId(location.getPatientId());
        Set<String> previousInside=(location.getInsideZoneIds()!=null)?
                new HashSet<>(location.getInsideZoneIds()):new HashSet<>();
        List<String> newInsideZone=new ArrayList<>();
        List<LocationEvent> events=new ArrayList<>();

        for(SafeZone zone:safeZones){
            double distance=GeoUtils.distanceInMeters(location.getLatitude(),location.getLongitude()
                    ,zone.getLatitude(),zone.getLongitude());
            boolean wasInside=previousInside.contains(zone.getId());

            boolean isInside=(wasInside)?distance<=(zone.getRadius()+exitBufferMeters):distance<=zone.getRadius();

            if(isInside){
                newInsideZone.add(zone.getId());
            }
            if(wasInside!=isInside){
                events.add(buildEvent(location,zone,(isInside)?LocationEventType.ENTER:LocationEventType.EXIT));
            }
        }
        if(!events.isEmpty()){
            locationEventRepo.saveAll(events);
        }
        location.setInsideZoneIds(newInsideZone);
    }

    public LocationEvent buildEvent(PatientLocation location, SafeZone zone, LocationEventType type){
        LocationEvent event=new LocationEvent();
        event.setPatientId(zone.getPatientId());
        event.setSafeZoneId(zone.getId());
        event.setZoneName(zone.getName());
        event.setType(type);
        event.setLatitude(location.getLatitude());
        event.setLongitude(location.getLongitude());
        event.setOccurredAt(LocalDateTime.now());
        return event;
    }
}
