package com.krishu.caretracev2.Controller;

import com.krishu.caretracev2.DTO.LocationEventResponse;
import com.krishu.caretracev2.DTO.LocationHistoryResponse;
import com.krishu.caretracev2.DTO.PatientLocationRequest;
import com.krishu.caretracev2.DTO.PatientLocationResponse;
import com.krishu.caretracev2.LocationEventType;
import com.krishu.caretracev2.Service.PatientLocationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/patient")
public class LocationController {

    private final PatientLocationService locationService;

    public LocationController(PatientLocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/me/location")
    public ResponseEntity<PatientLocationResponse> getMyLocation(Authentication authentication){
        return ResponseEntity.ok(locationService.getMyLocation(authentication));
    }

    @PutMapping("/me/updateLocation")
    public void updatePatientLocation(Authentication authentication,
                                      @RequestBody PatientLocationRequest request){
        locationService.updateLocation(authentication,request);
    }

    @GetMapping("/location/{patientId}")
    public ResponseEntity<PatientLocationResponse> getPatientLocation(@PathVariable String patientId
            ,Authentication authentication){
        return ResponseEntity.ok(locationService.
                getPatientLocation(patientId,authentication));
    }

    @GetMapping("/location/history/{patientId}")     // ?from=2026-11-27T08:00:00&to=2026-11-27T20:00:00
    public ResponseEntity<List<LocationHistoryResponse>> getLocationHistory(
            @PathVariable String patientId,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Authentication authentication){
        return ResponseEntity.ok(locationService.getLocationHistory(patientId,from,to,authentication));
    }

    @GetMapping("/location/events/{patientId}")
    public ResponseEntity<List<LocationEventResponse>> getLocationEvents(@PathVariable String patientId,@RequestParam(required=false)
    String zoneId,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime
            from,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                                                         Authentication authentication,
                                                                         @RequestParam(required=false) LocationEventType
                                                                                     type){
        return ResponseEntity.ok(locationService.getLocationEvents(patientId,zoneId,from,to,authentication,type));
    }
}
