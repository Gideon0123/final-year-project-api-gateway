package com.example.api_gateway.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(

        boolean success,
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp

) {
}