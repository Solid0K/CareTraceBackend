package com.krishu.caretracev2.Controller;

import com.krishu.caretracev2.DTO.PlaceGuesserAnswerRequest;
import com.krishu.caretracev2.DTO.PlaceGuesserAnswerResponse;
import com.krishu.caretracev2.DTO.PlaceGuesserQuestionResponse;
import com.krishu.caretracev2.DTO.PlaceGuesserSummaryResponse;
import com.krishu.caretracev2.Service.PlaceGuesserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/placeGuesser")
public class PlaceGuesserController {

    private final PlaceGuesserService gameService;

    public PlaceGuesserController(PlaceGuesserService gameService){
        this.gameService=gameService;
    }

    @PostMapping("/startGame")
    public ResponseEntity<PlaceGuesserQuestionResponse> startGame(Authentication authentication){
        return ResponseEntity.ok(gameService.startGame(authentication));
    }

    @PostMapping("/submitAnswe/{sessionId}")
    public ResponseEntity<PlaceGuesserAnswerResponse> submitAnswer(Authentication authentication
            , @PathVariable String sessionId,@RequestBody PlaceGuesserAnswerRequest request){
        return ResponseEntity.ok(gameService.submitAnswer(authentication,request,sessionId));
    }

    @GetMapping("/getSummary/{sessionId}")
    public ResponseEntity<PlaceGuesserSummaryResponse> getSummary(Authentication authentication
            ,@PathVariable String sessionId){
        return ResponseEntity.ok(gameService.getGameSummary(authentication,sessionId));
    }
}
