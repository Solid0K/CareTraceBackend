package com.krishu.caretracev2;

import com.krishu.caretracev2.CustomExceptions.*;
import com.krishu.caretracev2.DTO.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> UserAlreadyExists(UserAlreadyExistsException exp){
        ErrorResponse errorResponse=new ErrorResponse(409,exp.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> BadCredentials(BadCredentialsException exp){
        ErrorResponse errorResponse=new ErrorResponse(404,exp.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> NotFound(NotFoundException exp){
        ErrorResponse errorResponse=new ErrorResponse(404,exp.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> Unauthorized(UnauthorizedException exp){
        ErrorResponse errorResponse=new ErrorResponse(401,exp.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(NotRelatedException.class)
    public ResponseEntity<ErrorResponse> NotRelated(NotRelatedException exp){
        ErrorResponse errorResponse=new ErrorResponse(409,exp.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(PatientLocationNotKnowException.class)
    public ResponseEntity<ErrorResponse> PatientLocationNotKnow(PatientLocationNotKnowException exp){
        ErrorResponse errorResponse=new ErrorResponse(404,exp.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(AlreadyResolvedAlertException.class)
    public ResponseEntity<ErrorResponse> AlreadyResolvedAlert(AlreadyResolvedAlertException exp){
        ErrorResponse errorResponse=new ErrorResponse(409,exp.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(GameAlreadyCompletedException.class)
    public ResponseEntity<ErrorResponse> GameAlreadyCompleted(GameAlreadyCompletedException exp){
        ErrorResponse errorResponse=new ErrorResponse(409,exp.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(OutOfRangeException.class)
    public ResponseEntity<ErrorResponse> OutOfRange(OutOfRangeException exp){
        ErrorResponse errorResponse=new ErrorResponse(400,exp.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(GameNotCompleteException.class)
    public ResponseEntity<ErrorResponse> GameNotComplete(GameNotCompleteException exp){
        ErrorResponse errorResponse=new ErrorResponse(409,exp.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> InvalidFile(InvalidFileException exp){
        ErrorResponse errorResponse=new ErrorResponse(400,exp.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
}
