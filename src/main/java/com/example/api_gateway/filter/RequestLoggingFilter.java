package com.example.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class RequestLoggingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            WebFilterChain chain
    ) {

        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst("X-Correlation-ID");

        log.info(
                "Request: {} {} CorrelationId={}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI(),
                correlationId
        );

        return chain.filter(exchange);
    }
}
