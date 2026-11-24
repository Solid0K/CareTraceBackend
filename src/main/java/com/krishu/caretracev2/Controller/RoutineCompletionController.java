package com.krishu.caretracev2.Controller;

import com.krishu.caretracev2.ActivityType;
import com.krishu.caretracev2.CompletionStatus;
import com.krishu.caretracev2.DTO.ActivityRequest;
import com.krishu.caretracev2.DTO.ActivityResponse;
import com.krishu.caretracev2.Service.RoutineCompletionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/routineCompletion")
public class RoutineCompletionController {

    private final RoutineCompletionService routineCompletionService;

    public RoutineCompletionController(RoutineCompletionService routineCompletionService){
        this.routineCompletionService=routineCompletionService;
    }

    @GetMapping("/schedule/me")
    public ResponseEntity<List<ActivityResponse>> getMySchedule(@RequestParam(required=false)
                                                                    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE)
                                                                    LocalDate date, Authentication authentication){
        return ResponseEntity.ok(routineCompletionService.getMySchedule(date,authentication));
    }

    @GetMapping("/schedule/{patientId}")
    public ResponseEntity<List<ActivityResponse>> getSchedule(@PathVariable String patientId,
                                                              @RequestParam(required=false)
                                                              @DateTimeFormat(iso= DateTimeFormat.ISO.DATE)
                                                              LocalDate date,
                                                              Authentication authentication){
        return ResponseEntity.ok(routineCompletionService.getSchedule(patientId,date,authentication));
    }

    @PutMapping("/completeActivity/{activityId}")
    public ResponseEntity<ActivityResponse> completeActivity(@PathVariable String activityId,
                                                             @RequestBody(required=false) ActivityRequest request,
                                                             Authentication authentication){
        ActivityRequest safeRequest=(request!=null)?request:new ActivityRequest();
        return ResponseEntity.ok(routineCompletionService.completeActivity(activityId,safeRequest,authentication));
    }

    @PutMapping("/confirmActivity/{activityId}")
    public ResponseEntity<ActivityResponse> confirmActivity(@PathVariable String activityId,
                                                            Authentication authentication){
        return ResponseEntity.ok(routineCompletionService.confirmedActivity(activityId,authentication));
    }

    @GetMapping("/history/me")
    public ResponseEntity<List<ActivityResponse>> getMyHistory(
            @RequestParam(required=false) CompletionStatus status,
            @RequestParam(required=false) ActivityType activityType,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication){
        return ResponseEntity.ok(routineCompletionService.getMyHistory(activityType,status,from,to,authentication));
    }

    @GetMapping("/history/{patientId}")
    public ResponseEntity<List<ActivityResponse>> getHistory(
            @PathVariable String patientId,
            @RequestParam(required=false) CompletionStatus status,
            @RequestParam(required=false) ActivityType activityType,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication){
        return ResponseEntity.ok(routineCompletionService.getHistory(patientId,activityType,status,from,to,
                authentication));
    }
}
