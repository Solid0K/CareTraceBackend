package com.krishu.caretracev2.Controller;

import com.krishu.caretracev2.DTO.ImportantPersonRequest;
import com.krishu.caretracev2.DTO.ImportantPersonResponse;
import com.krishu.caretracev2.Service.ImportantPersonService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/importantPerson")
public class ImportantPersonController {

    private final ImportantPersonService importantPersonService;

    public ImportantPersonController(ImportantPersonService importantPersonService) {
        this.importantPersonService = importantPersonService;
    }

    @PostMapping("/addPerson/{patientId}")
    public ResponseEntity<ImportantPersonResponse> addImportantPerson(@RequestPart("request") ImportantPersonRequest request
            , @PathVariable String patientId, Authentication authentication,@RequestPart("photo") MultipartFile photo) throws IOException {
        return ResponseEntity.ok(importantPersonService.createImportantPerson(request,patientId,photo,authentication));
    }

    @PostMapping("/addPersonWithoutPhoto/{patientId}")
    public ResponseEntity<ImportantPersonResponse> addImportantPersonWithOutPhoto(@RequestBody ImportantPersonRequest request,@PathVariable String patientId,
                                                                                  Authentication authentication) throws IOException {
        return ResponseEntity.ok(importantPersonService.createImportantPerson(request,patientId,null,authentication));
    }


    @GetMapping("/getPersons/{patientId}")
    public ResponseEntity<List<ImportantPersonResponse>> getAllImportantPerson(@PathVariable String patientId,
                                                                               Authentication authentication){
        return ResponseEntity.ok(importantPersonService.getImportantPersons(patientId,authentication));
    }

    @PutMapping("/updatePerson/{patientId}/{importantPersonId}")
    public ResponseEntity<ImportantPersonResponse> updateImportantPerson(@RequestPart ImportantPersonRequest request,@PathVariable String patientId,
                                                                         @PathVariable String importantPersonId,
                                                                         @RequestPart MultipartFile photo,
                                                                         Authentication authentication) throws IOException {
        return ResponseEntity.ok(importantPersonService.updatePerson(request,patientId,importantPersonId,authentication,photo));
    }

    @DeleteMapping("/delete/{patientId}/{importantPersonId}")
    public void deleteImportantPerson(@PathVariable String patientId,@PathVariable
    String importantPersonId,Authentication authentication){
        importantPersonService.deletePerson(importantPersonId,patientId,authentication);
    }

    @GetMapping("/patient/me")
    public ResponseEntity<List<ImportantPersonResponse>> getImportantPersonForPatient(Authentication authentication){
        return ResponseEntity.ok(importantPersonService.getPatientImportantPerson(authentication));
    }
}
