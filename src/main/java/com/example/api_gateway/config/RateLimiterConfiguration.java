package com.example.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfiguration {

    @Bean
    public RedisRateLimiter defaultRedisRateLimiter() {

        return new RedisRateLimiter(
                20,
                40
        );

    }

}