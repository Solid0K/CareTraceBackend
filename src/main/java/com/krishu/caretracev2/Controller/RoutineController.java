package com.krishu.caretracev2.Controller;

import com.krishu.caretracev2.DTO.RoutineRequest;
import com.krishu.caretracev2.DTO.RoutineResponse;
import com.krishu.caretracev2.Service.RoutineService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routine")
public class RoutineController {

    private final RoutineService routineService;

    public RoutineController(RoutineService routineService) {
        this.routineService = routineService;
    }

    @PostMapping("/addRoutine/{patientId}")
    public ResponseEntity<RoutineResponse> createRoutine(@RequestBody RoutineRequest request, @PathVariable String patientId, Authentication authentication){
        return ResponseEntity.ok(routineService.createRoutine(request,patientId,authentication));
    }

    @GetMapping("/getRoutines/{patientId}")
    public ResponseEntity<List<RoutineResponse>> getPatientRoutines(@PathVariable String patientId,Authentication authentication){
        return ResponseEntity.ok(routineService.getPatientsRoutine(patientId,authentication));
    }

    @PutMapping("/updateRoutine/{routineId}/{patientId}")
    public ResponseEntity<RoutineResponse> updateRoutine(@RequestBody RoutineRequest request,@PathVariable String routineId,@PathVariable String patientId,
                                                         Authentication authentication){
        return ResponseEntity.ok(routineService.updateRoutine(request,routineId,patientId,authentication));
    }

    @DeleteMapping("/deleteRoutine/{routineId}/{patientId}")
    public void deleteRoutine(@PathVariable String routineId,@PathVariable String patientId,Authentication authentication){
        routineService.deleteRoutine(routineId,patientId,authentication);
    }

    @GetMapping("/getPatientRoutine")
    public ResponseEntity<List<RoutineResponse>> getRoutineForPatient(Authentication authentication){
        return ResponseEntity.ok(routineService.getPatientForRoutine(authentication));
    }
}
