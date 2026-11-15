package com.krishu.caretracev2.CustomExceptions;

public class MissingRoutineTimeException extends RuntimeException {
    public MissingRoutineTimeException(String message) {
        super(message);
    }
}
