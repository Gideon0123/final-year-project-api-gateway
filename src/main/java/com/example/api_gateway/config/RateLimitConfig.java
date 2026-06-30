package com.example.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Configuration
public class RateLimitConfig {

    @Bean
    public KeyResolver userKeyResolver() {

        return exchange -> {

            ServerHttpRequest request = exchange.getRequest();

            String userId = request.getHeaders().getFirst("X-User-Id");

            if (userId != null && !userId.isBlank()) {
                return Mono.just(userId);
            }

            InetSocketAddress remoteAddress = request.getRemoteAddress();

            if (remoteAddress != null && remoteAddress.getAddress() != null) {
                return Mono.just(remoteAddress.getAddress().getHostAddress());
            }

            return Mono.just("unknown-ip");
        };
    }

}