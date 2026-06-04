package com.example.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class CorrelationIdFilter implements WebFilter {

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            WebFilterChain chain
    ) {

        String correlationId = Optional.ofNullable(
                exchange.getRequest()
                        .getHeaders()
                        .getFirst("X-Correlation-ID")

        ).orElse(
                UUID.randomUUID().toString()
        );


        ServerHttpRequest request = exchange.getRequest()
                .mutate()
                .header(
                        "X-Correlation-ID",
                        correlationId
                )
                .build();

        exchange.getResponse()
                .getHeaders()
                .add(
                        "X-Correlation-ID",
                        correlationId
                );

        return chain.filter(
                exchange.mutate()
                        .request(request)
                        .build()
        );
    }
}
