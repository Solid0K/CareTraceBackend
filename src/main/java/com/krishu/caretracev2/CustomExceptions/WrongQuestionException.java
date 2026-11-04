package com.krishu.caretracev2.CustomExceptions;

public class WrongQuestionException extends RuntimeException {
    public WrongQuestionException(String message) {
        super(message);
    }
}
