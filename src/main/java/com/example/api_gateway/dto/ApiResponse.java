package com.example.api_gateway.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ApiResponse<T>(

        boolean success,
        String message,
        int status,
        T data,
        LocalDateTime timestamp
) {

    public static <T> ApiResponse<T> success(
            String message,
            T data,
            int status
    ) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .status(status)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> failure(
            String message,
            int status
    ) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .status(status)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }
}