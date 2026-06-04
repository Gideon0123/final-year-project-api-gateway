package com.example.api_gateway.security;

import com.example.api_gateway.util.GatewayHeaders;
import com.example.api_gateway.util.ResponseWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtService jwtService;
    private final ResponseWriter responseWriter;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(

            "/auth/login",
            "/auth/register",
            "/auth/verify-email",
            "/auth/forgot-password",
            "/auth/reset-password",
            "/auth/refresh-token",

            "/actuator",
            "/actuator/health",
            "/actuator/info"
    );

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            WebFilterChain chain
    ) {

        String path = exchange.getRequest()
                .getURI()
                .getPath();

        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        String token = extractToken(exchange);

        if (token == null) {

            log.warn("No JWT token found for request: {}", path);

            return unauthorized(exchange, "Authentication token is required");
        }

        try {
            if (!jwtService.validateToken(token)) {

                log.warn("Invalid JWT token for request: {}", path);

                return responseWriter.writeError(
                        exchange,
                        HttpStatus.UNAUTHORIZED,
                        "Invalid or expired token"
                );
            }

            String username = jwtService.extractUsername(token);

            Long userId = jwtService.extractUserId(token);

            String role = jwtService.extractRole(token);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(
                                    new SimpleGrantedAuthority(
                                            "ROLE_" + role
                                    )
                            )
                    );

            ServerHttpRequest modifiedRequest = exchange.getRequest()
                    .mutate()
                    .header(GatewayHeaders.USER_ID, String.valueOf(userId))
                    .header(GatewayHeaders.USER_EMAIL, username)
                    .header(GatewayHeaders.USER_ROLE, role)

                    .build();

            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(modifiedRequest)
                    .build();

            return chain.filter(modifiedExchange)
                    .contextWrite(
                            ReactiveSecurityContextHolder
                                    .withAuthentication(
                                            authentication
                                    )
                    );

        }

        catch (Exception ex) {
            log.error("JWT Authentication error", ex);

            return unauthorized(exchange, "Authentication failed");
        }
    }

    private boolean isPublicEndpoint(
            String path
    ) {

        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(path::startsWith);
    }

    private String extractToken(
            ServerWebExchange exchange
    ) {

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(
                        HttpHeaders.AUTHORIZATION
                );

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            return authHeader.substring(7);
        }

        HttpCookie cookie = exchange.getRequest()
                .getCookies()
                .getFirst("accessToken");

        if (cookie != null) {
            return cookie.getValue();
        }

        return null;
    }

    private Mono<Void> unauthorized(
            ServerWebExchange exchange,
            String message
    ) {
        exchange.getResponse()
                .setStatusCode(HttpStatus.UNAUTHORIZED);

        exchange.getResponse()
                .getHeaders()
                .add("X-ERROR-MESSAGE", message);

        return exchange.getResponse().setComplete();
    }
}