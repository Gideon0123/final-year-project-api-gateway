package com.example.api_gateway.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gateway.rate-limit")
@Getter
@Setter
public class RateLimitProperties {

    private Endpoint search = new Endpoint();

    private Endpoint upload = new Endpoint();

    private Endpoint download = new Endpoint();

    private Endpoint paper = new Endpoint();

    private Endpoint category = new Endpoint();

    @Getter
    @Setter
    public static class Endpoint {

        private int replenishRate;

        private int burstCapacity;

    }

}