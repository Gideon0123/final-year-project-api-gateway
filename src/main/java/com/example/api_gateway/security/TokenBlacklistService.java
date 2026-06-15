package com.example.api_gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final ReactiveRedisTemplate<String,Object> redisTemplate;

    public Mono<Boolean> blacklistToken(
            String token,
            Duration duration
    ) {

        return redisTemplate
                .opsForValue()
                .set(
                        token,
                        "BLACKLISTED",
                        duration
                );
    }

    public Mono<Boolean> isBlacklisted(
            String token
    ) {

        return redisTemplate.hasKey(token);
    }
}