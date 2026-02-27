package com.airlinemanagementsystem.flight.exception;

// Base exception for business logic errors
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
