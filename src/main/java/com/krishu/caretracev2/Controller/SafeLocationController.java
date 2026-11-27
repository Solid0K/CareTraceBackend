package com.krishu.caretracev2.Controller;

import com.krishu.caretracev2.DTO.SafeLocationRequest;
import com.krishu.caretracev2.DTO.SafeLocationResponse;
import com.krishu.caretracev2.Service.SafeZoneService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/safelocation")
public class SafeLocationController {

    private final SafeZoneService safeLocationService;

    public SafeLocationController(SafeZoneService safeLocationService) {
        this.safeLocationService = safeLocationService;
    }

    @PostMapping("/createSafeLocation/{patientId}")
    public ResponseEntity<SafeLocationResponse> createSafeLocation(@PathVariable String patientId,
                                                                   @RequestBody SafeLocationRequest request
            , Authentication authentication){
        return ResponseEntity.ok(safeLocationService.createSafeZone(patientId,request,authentication));
    }

    @GetMapping("/getSafeZones/{patientId}")
    public ResponseEntity<List<SafeLocationResponse>> getPatientSafeLocation(@PathVariable String patientId,
                                                                             Authentication authentication){
        return ResponseEntity.ok(safeLocationService.getSafeZones(patientId,authentication));
    }

    @PutMapping("/updateSafeLocation/{safeZoneId}/{patientId}")
    public ResponseEntity<SafeLocationResponse> updateSafeLocation(@PathVariable String patientId,@PathVariable String safeZoneId
            ,@RequestBody SafeLocationResponse request, Authentication authentication){
        return ResponseEntity.ok(safeLocationService.updateSafeZone(patientId,safeZoneId,request,authentication));
    }

    @DeleteMapping("/deleteSafeLocation/{safeZoneId}/{patientId}")
    public void deleteSafeLocation(@PathVariable String patientId,@PathVariable String safeZoneId, Authentication authentication){
        safeLocationService.deleteSafeZone(patientId,safeZoneId,authentication);
    }

    @GetMapping("/me")
    public ResponseEntity<List<SafeLocationResponse>> getMySafeLocation(Authentication authentication){
        return ResponseEntity.ok(safeLocationService.getMySafeZones(authentication));
    }
}
