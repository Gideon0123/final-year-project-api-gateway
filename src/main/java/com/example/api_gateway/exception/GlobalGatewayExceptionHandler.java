package com.example.api_gateway.exception;

import com.example.api_gateway.util.ResponseWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class GlobalGatewayExceptionHandler
        implements ErrorWebExceptionHandler {

    private final ResponseWriter responseWriter;

    @Override
    public Mono<Void> handle(
            ServerWebExchange exchange,
            Throwable ex
    ) {
        log.error(
                "Gateway Exception",
                ex
        );

        HttpStatus status = switch (ex) {

            case ResponseStatusException responseEx ->
                    (HttpStatus) responseEx.getStatusCode();

            case AuthenticationException ignored -> HttpStatus.UNAUTHORIZED;

            case AccessDeniedException ignored -> HttpStatus.FORBIDDEN;

            case ConnectException ignored -> HttpStatus.SERVICE_UNAVAILABLE;

            case TimeoutException ignored -> HttpStatus.GATEWAY_TIMEOUT;

            case IllegalArgumentException ignored -> HttpStatus.BAD_REQUEST;

            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        String message = switch (status) {

            case UNAUTHORIZED -> "Authentication failed";

            case FORBIDDEN -> "Access denied";

            case SERVICE_UNAVAILABLE -> "Requested service is unavailable";

            case GATEWAY_TIMEOUT -> "Request timed out";

            case BAD_REQUEST -> "Invalid request";

            default -> "Unexpected gateway error";
        };

        return responseWriter.writeError(
                exchange,
                status,
                message
        );
    }
}
