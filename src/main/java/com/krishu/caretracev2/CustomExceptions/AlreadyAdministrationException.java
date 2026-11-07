package com.krishu.caretracev2.CustomExceptions;

public class AlreadyAdministrationException extends RuntimeException {
    public AlreadyAdministrationException(String message) {
        super(message);
    }
}
