package com.example.api_gateway.util;

import com.example.api_gateway.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ResponseWriter {

    private final ObjectMapper objectMapper;

    public Mono<Void> writeError(
            ServerWebExchange exchange,
            HttpStatus status,
            String message
    ) {

        try {
            ErrorResponse response = ErrorResponse.builder()

                    .success(false)
                    .status(status.value())
                    .error(status.getReasonPhrase())
                    .message(message)
                    .path(
                            exchange.getRequest()
                                    .getPath()
                                    .value()
                    )
                    .timestamp(LocalDateTime.now())
                    .build();

            byte[] bytes = objectMapper.writeValueAsBytes(response);

            exchange.getResponse().setStatusCode(status);

            exchange.getResponse()
                    .getHeaders()
                    .setContentType(MediaType.APPLICATION_JSON);

            DataBuffer buffer = exchange.getResponse()
                    .bufferFactory()
                    .wrap(bytes);

            return exchange.getResponse()
                    .writeWith(Mono.just(buffer));

        }

        catch (Exception ex) {

            return exchange.getResponse().setComplete();
        }
    }
}