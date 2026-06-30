package com.example.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisRateLimiterConfig {

    /*
     * SEARCH
     * 20 req/sec
     * burst 40
     */
    @Bean
    public RedisRateLimiter searchRateLimiter() {
        return new RedisRateLimiter(20, 40);
    }

    /*
     * FILE UPLOAD
     * 2 req/sec
     * burst 5
     */
    @Bean
    public RedisRateLimiter uploadRateLimiter() {
        return new RedisRateLimiter(2, 5);
    }

    /*
     * DOWNLOAD
     * 5 req/sec
     * burst 10
     */
    @Bean
    public RedisRateLimiter downloadRateLimiter() {
        return new RedisRateLimiter(5, 10);
    }

    /*
     * PAPER CRUD
     */
    @Bean
    public RedisRateLimiter paperRateLimiter() {
        return new RedisRateLimiter(5, 10);
    }

    /*
     * CATEGORY CRUD
     */
    @Bean
    public RedisRateLimiter categoryRateLimiter() {
        return new RedisRateLimiter(2, 5);
    }

}