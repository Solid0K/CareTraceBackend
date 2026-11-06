package com.krishu.caretracev2.Controller;

import com.krishu.caretracev2.DTO.PatientMakingRequest;
import com.krishu.caretracev2.DTO.PatientResponse;
import com.krishu.caretracev2.DTO.PatientUpdateRequest;
import com.krishu.caretracev2.Service.PatientService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping("/makeAccount")
    public ResponseEntity<PatientResponse> createPatient(@RequestBody PatientMakingRequest request, Authentication authentication){
        return ResponseEntity.ok(patientService.createPatient(request,authentication));
    }

    @GetMapping("/getPatient/{patientId}")
    public ResponseEntity<PatientResponse> getPatient(@PathVariable String patientId,Authentication authentication){
        return ResponseEntity.ok(patientService.getPatient(patientId,authentication));
    }

    @GetMapping("/getAllPatient")
    public ResponseEntity<List<PatientResponse>> getAllPatient(Authentication authentication){
        return ResponseEntity.ok(patientService.getAllPatient(authentication));
    }

    @PutMapping("/updatePatient/{patientId}")
    public ResponseEntity<PatientResponse> updatePatient(@PathVariable String patientId,@RequestBody PatientUpdateRequest request, Authentication authentication){
        return ResponseEntity.ok(patientService.updatePatient(patientId,request,authentication));
    }

    @GetMapping("/me")
    public ResponseEntity<PatientResponse> patientOwnInfo(Authentication authentication){
        return ResponseEntity.ok(patientService.getPatientOwnInfo(authentication));
    }

    @PutMapping(value="/updatePhoto/{patientId}", consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PatientResponse> updatePhoto(@PathVariable String patientId,
                                                       @RequestPart("photo") MultipartFile photo, Authentication authentication){
        return ResponseEntity.ok(patientService.updatePatientPhoto(patientId,authentication,photo));
    }

    @DeleteMapping("/deletePhoto/{patientId}")
    public ResponseEntity<Void> deletePhoto(@PathVariable String patientId, Authentication authentication){
        patientService.deletePatientPhoto(patientId,authentication);
        return ResponseEntity.noContent().build();
    }
}
