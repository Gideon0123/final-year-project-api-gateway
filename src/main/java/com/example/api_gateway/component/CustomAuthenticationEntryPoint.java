package com.example.api_gateway.component;

import com.example.api_gateway.util.ResponseWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final ResponseWriter responseWriter;

    @Override
    public Mono<Void> commence(
            ServerWebExchange exchange,
            AuthenticationException ex
    ) {

        return responseWriter.writeError(
                exchange,
                HttpStatus.UNAUTHORIZED,
                "Authentication required"
        );
    }
}
