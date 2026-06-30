package com.example.api_gateway.config;

import com.example.api_gateway.util.RateLimitProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RateLimiterConfiguration {

    private final RateLimitProperties properties;

    @Bean
    public RedisRateLimiter searchRateLimiter() {

        return new RedisRateLimiter(

                properties.getSearch().getReplenishRate(),

                properties.getSearch().getBurstCapacity()

        );

    }

    @Bean
    public RedisRateLimiter uploadRateLimiter() {

        return new RedisRateLimiter(

                properties.getUpload().getReplenishRate(),

                properties.getUpload().getBurstCapacity()

        );

    }

    @Bean
    public RedisRateLimiter downloadRateLimiter() {

        return new RedisRateLimiter(

                properties.getDownload().getReplenishRate(),

                properties.getDownload().getBurstCapacity()

        );

    }

    @Bean
    public RedisRateLimiter paperRateLimiter() {

        return new RedisRateLimiter(

                properties.getPaper().getReplenishRate(),

                properties.getPaper().getBurstCapacity()

        );

    }

    @Bean
    public RedisRateLimiter categoryRateLimiter() {

        return new RedisRateLimiter(

                properties.getCategory().getReplenishRate(),

                properties.getCategory().getBurstCapacity()

        );

    }

}