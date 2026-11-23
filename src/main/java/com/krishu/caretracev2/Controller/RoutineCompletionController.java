package com.krishu.caretracev2.Controller;

import com.krishu.caretracev2.DTO.ActivityResponse;
import com.krishu.caretracev2.Service.RoutineCompletionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/routineCompletion")
public class RoutineCompletionController {

    private final RoutineCompletionService routineCompletionService;

    public RoutineCompletionController(RoutineCompletionService routineCompletionService){
        this.routineCompletionService=routineCompletionService;
    }
}
