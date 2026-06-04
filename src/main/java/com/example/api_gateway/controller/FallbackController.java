package com.example.api_gateway.controller;

import com.example.api_gateway.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public ResponseEntity<ApiResponse<Void>> authFallback() {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(
                        ApiResponse.failure(
                                "Authentication service is temporarily unavailable",
                                HttpStatus.SERVICE_UNAVAILABLE.value()
                        )
                );
    }

    @GetMapping("/research")
    public ResponseEntity<ApiResponse<Void>> researchFallback() {

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(
                        ApiResponse.failure(
                                "Research service is temporarily unavailable",
                                HttpStatus.SERVICE_UNAVAILABLE.value()
                        )
                );
    }

    @GetMapping("/review")
    public ResponseEntity<ApiResponse<Void>> reviewFallback() {

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(
                        ApiResponse.failure(
                                "Review service is temporarily unavailable",
                                HttpStatus.SERVICE_UNAVAILABLE.value()
                        )
                );
    }

    @GetMapping("/notification")
    public ResponseEntity<ApiResponse<Void>> notificationFallback() {

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(
                        ApiResponse.failure(
                                "Notification service is temporarily unavailable",
                                HttpStatus.SERVICE_UNAVAILABLE.value()
                        )
                );
    }

    @GetMapping("/plagiarism")
    public ResponseEntity<ApiResponse<Void>> plagiarismFallback() {

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(
                        ApiResponse.failure(
                                "Plagiarism service is temporarily unavailable",
                                HttpStatus.SERVICE_UNAVAILABLE.value()
                        )
                );
    }
}
