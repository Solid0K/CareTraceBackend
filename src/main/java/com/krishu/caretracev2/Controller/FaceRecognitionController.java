package com.krishu.caretracev2.Controller;

import com.krishu.caretracev2.DTO.FaceRecognitionResponse;
import com.krishu.caretracev2.Service.FaceRecognitionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/face")
public class FaceRecognitionController {

    private final FaceRecognitionService faceRecognitionService;

    public FaceRecognitionController(FaceRecognitionService faceRecognitionService){
        this.faceRecognitionService=faceRecognitionService;
    }

    @PostMapping("/getPerson")
    public ResponseEntity<FaceRecognitionResponse> matchFaces(Authentication authentication
            ,@RequestParam("Photo") MultipartFile photo) throws IOException {
        byte[] image=photo.getBytes();
        FaceRecognitionResponse response=faceRecognitionService.recognisePerson(authentication,image);
        if(response==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
}
