package com.example.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisRateLimiterConfig {

    @Bean
    public RedisRateLimiter redisRateLimiter(
            ApplicationContext applicationContext
    ) {

        RedisRateLimiter rateLimiter =
                new RedisRateLimiter(20, 50);

        rateLimiter.setApplicationContext(applicationContext);

        return rateLimiter;
    }
}