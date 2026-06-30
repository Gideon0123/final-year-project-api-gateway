package com.example.api_gateway.exception;

public class TimeoutException extends RuntimeException {
    public TimeoutException(String message) {
        super(message);
    }
}
