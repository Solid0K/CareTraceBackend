package com.krishu.caretracev2.Controller;

import com.krishu.caretracev2.AdministrationStatus;
import com.krishu.caretracev2.DTO.MarkTakenRequest;
import com.krishu.caretracev2.DTO.MedicationAdministrationResponse;
import com.krishu.caretracev2.Service.MedicationAdministrationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/medication")
public class MedicationAdministrationController {

    private final MedicationAdministrationService medicationAdminService;

    public MedicationAdministrationController(MedicationAdministrationService medicationAdminService){
        this.medicationAdminService=medicationAdminService;
    }

    @PutMapping("/taken/{administrationId}")
    public ResponseEntity<MedicationAdministrationResponse> markAsTaken(@PathVariable String administrationId,
                                                                        Authentication authentication,
                                                                        @RequestBody MarkTakenRequest request){
        return ResponseEntity.ok(medicationAdminService.markAsTaken(administrationId,authentication,request));
    }

    @GetMapping("/me")
    public ResponseEntity<List<MedicationAdministrationResponse>> getMyDoses(
            @RequestParam(required=false) AdministrationStatus status,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication){
        return ResponseEntity.ok(medicationAdminService.getMyDosage(status,from,to,authentication));
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<List<MedicationAdministrationResponse>> getHistory(@PathVariable String patientId,
                                                                             @RequestParam(required=false)
                                                                             AdministrationStatus status,
                                                                             @RequestParam(required=false) String
                                                                                         medicationId,
                                                                             @RequestParam(required=false)
                                                                                 @DateTimeFormat(iso=DateTimeFormat.
                                                                                         ISO.DATE)
                                                                                 LocalDate from,
                                                                             @RequestParam(required=false)
                                                                                 @DateTimeFormat(iso=DateTimeFormat.
                                                                                         ISO.DATE) LocalDate to,
                                                                             Authentication authentication){
        return ResponseEntity.ok(medicationAdminService.getHistory(patientId,status,medicationId,from,to,
                authentication));
    }
}
