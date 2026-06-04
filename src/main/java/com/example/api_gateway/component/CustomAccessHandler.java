package com.example.api_gateway.component;

import com.example.api_gateway.util.ResponseWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CustomAccessHandler implements ServerAccessDeniedHandler {

    private final ResponseWriter responseWriter;

    @Override
    public Mono<Void> handle(
            ServerWebExchange exchange,
            AccessDeniedException ex
    ) {

        return responseWriter.writeError(
                exchange,
                HttpStatus.FORBIDDEN,
                "You do not have permission to access this resource"
        );
    }
}
