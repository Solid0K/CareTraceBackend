package com.krishu.caretracev2.Service;

import com.krishu.caretracev2.CustomExceptions.NotFoundException;
import com.krishu.caretracev2.DTO.GeoFenceResponse;
import com.krishu.caretracev2.DTO.SafeLocationStatus;
import com.krishu.caretracev2.GeoUtils;
import com.krishu.caretracev2.Model.PatientLocation;
import com.krishu.caretracev2.Model.SafeZone;
import com.krishu.caretracev2.Repository.PatientLocationRepo;
import com.krishu.caretracev2.Repository.SafeZoneRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeoFencingService {

    private final PatientLocationRepo patientLocationRepo;
    private final SafeZoneRepo safeLocationRepo;

    public GeoFencingService(PatientLocationRepo patientLocationRepo, SafeZoneRepo safeLocationRepo) {
        this.patientLocationRepo = patientLocationRepo;
        this.safeLocationRepo = safeLocationRepo;
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
}
