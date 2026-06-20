package com.example.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {

        return builder.routes()

                .route("auth-service",
                        r -> r.path("/auth/**", "/users/**", "/admin/users/**")
                                .uri("lb://AUTH-SERVICE"))

                .route("research-service",
                        r -> r.path("/research/**")
                                .uri("lb://RESEARCH-SERVICE"))

                .route("review-service",
                        r -> r.path("/review/**")
                                .uri("lb://REVIEW-SERVICE"))

                .route("plagiarism-service",
                        r -> r.path("/plagiarism/**")
                                .uri("lb://PLAGIARISM-SERVICE"))

                .route("notification-service",
                        r -> r.path("/notification/**", "/test/email")
                                .uri("lb://NOTIFICATION-SERVICE"))

                .build();
    }
}