package com.example.api_gateway.exception;

public class ConnectException extends RuntimeException {
    public ConnectException(String message) {
        super(message);
    }
}
