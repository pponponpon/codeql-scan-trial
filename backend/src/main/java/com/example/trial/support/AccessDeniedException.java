package com.example.trial.support;

public class AccessDeniedException extends RuntimeException {

    private final int status;

    public AccessDeniedException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
