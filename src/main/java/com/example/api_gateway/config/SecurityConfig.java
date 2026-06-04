package com.example.api_gateway.config;

import com.example.api_gateway.component.CustomAccessHandler;
import com.example.api_gateway.component.CustomAuthenticationEntryPoint;
import com.example.api_gateway.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAccessHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http
    ) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange

                        .pathMatchers(
                                "/auth/**",
                                "/actuator/**"
                        )
                        .permitAll()

                        // ADMIN ONLY
                        .pathMatchers(
                                "/review/admin/**"
                        )
                        .hasRole("ADMIN")

                        // REVIEWER
                        .pathMatchers(
                                "/review/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "REVIEWER"
                        )

                        // RESEARCHERS
                        .pathMatchers(
                                "/research/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "RESEARCHER"
                        )

                        .anyExchange()
                        .authenticated()
                )
                .addFilterAt(
                        jwtAuthenticationFilter,
                        SecurityWebFiltersOrder.AUTHENTICATION
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .build();
    }
}