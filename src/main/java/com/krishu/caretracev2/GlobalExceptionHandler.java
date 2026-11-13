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
    public ResponseEntity<ErrorResponse> userAlreadyExists(
            UserAlreadyExistsException exp) {

        ErrorResponse error = new ErrorResponse(409, exp.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> badCredentials(
            BadCredentialsException exp) {

        ErrorResponse error = new ErrorResponse(401, exp.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(NotFoundException exp) {

        ErrorResponse error = new ErrorResponse(404, exp.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> unauthorized(
            UnauthorizedException exp) {

        ErrorResponse error = new ErrorResponse(401, exp.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(NotRelatedException.class)
    public ResponseEntity<ErrorResponse> notRelated(NotRelatedException exp) {

        ErrorResponse error = new ErrorResponse(403, exp.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(PatientLocationNotKnowException.class)
    public ResponseEntity<ErrorResponse> patientLocationNotKnow(
            PatientLocationNotKnowException exp) {

        ErrorResponse error = new ErrorResponse(404, exp.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AlreadyResolvedAlertException.class)
    public ResponseEntity<ErrorResponse> alreadyResolvedAlert(
            AlreadyResolvedAlertException exp) {

        ErrorResponse error = new ErrorResponse(409, exp.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(GameAlreadyCompletedException.class)
    public ResponseEntity<ErrorResponse> gameAlreadyCompleted(
            GameAlreadyCompletedException exp) {

        ErrorResponse error = new ErrorResponse(409, exp.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(OutOfRangeException.class)
    public ResponseEntity<ErrorResponse> outOfRange(OutOfRangeException exp) {

        ErrorResponse error = new ErrorResponse(400, exp.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(GameNotCompleteException.class)
    public ResponseEntity<ErrorResponse> gameNotComplete(
            GameNotCompleteException exp) {

        ErrorResponse error = new ErrorResponse(409, exp.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> invalidFile(InvalidFileException exp) {

        ErrorResponse error = new ErrorResponse(400, exp.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(WrongQuestionException.class)
    public ResponseEntity<ErrorResponse> wrongQuestion(WrongQuestionException exp) {

        ErrorResponse error = new ErrorResponse(400, exp.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AlreadyAdministrationException.class)
    public ResponseEntity<ErrorResponse> alreadyAdministration(
            AlreadyAdministrationException exp) {

        ErrorResponse error = new ErrorResponse(409, exp.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
