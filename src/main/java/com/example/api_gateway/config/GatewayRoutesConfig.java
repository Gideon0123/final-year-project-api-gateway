package com.example.api_gateway.config;


import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@Configuration
@RequiredArgsConstructor
public class GatewayRoutesConfig {

    private final KeyResolver userKeyResolver;

    private final RedisRateLimiter searchRateLimiter;
    private final RedisRateLimiter uploadRateLimiter;
    private final RedisRateLimiter downloadRateLimiter;
    private final RedisRateLimiter paperRateLimiter;
    private final RedisRateLimiter categoryRateLimiter;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {

        return builder.routes()

                /*
                 * ==========================================================
                 * AUTH SERVICE
                 * ==========================================================
                 */
                .route("auth-service",
                        r -> r.path(
                                        "/auth/**",
                                        "/users/**",
                                        "/admin/users/**"
                                )
                                .filters(f -> f
                                        .circuitBreaker(c -> c
                                                .setName("authCircuitBreaker")
                                                .setFallbackUri("forward:/fallback/auth")))
                                .uri("lb://AUTH-SERVICE"))

                /*
                 * ==========================================================
                 * RESEARCH SEARCH
                 * ==========================================================
                 */
                .route("research-search",
                        r -> r.path(
                                        "/research/papers/search",
                                        "/research/categories/search"
                                )
                                .and()
                                .method(HttpMethod.GET)
                                .filters(f -> f

                                        .circuitBreaker(c -> c
                                                .setName("researchCircuitBreaker")
                                                .setFallbackUri("forward:/fallback/research"))

                                        .requestRateLimiter(config -> {
                                            config.setKeyResolver(userKeyResolver);
                                            config.setRateLimiter(searchRateLimiter);
                                        })

                                        .retry(retry -> retry
                                                .setRetries(3)
                                                .setMethods(HttpMethod.GET)
                                                .setStatuses(HttpStatus.BAD_GATEWAY,
                                                        HttpStatus.GATEWAY_TIMEOUT,
                                                        HttpStatus.SERVICE_UNAVAILABLE))
                                )
                                .uri("lb://RESEARCH-SERVICE"))

                /*
                 * ==========================================================
                 * PAPER UPLOAD
                 * ==========================================================
                 */
                .route("research-upload",
                        r -> r.path("/research/papers/*/upload")
                                .and()
                                .method(HttpMethod.POST)
                                .filters(f -> f

                                        .circuitBreaker(c -> c
                                                .setName("researchCircuitBreaker")
                                                .setFallbackUri("forward:/fallback/research"))

                                        .requestRateLimiter(config -> {
                                            config.setKeyResolver(userKeyResolver);
                                            config.setRateLimiter(uploadRateLimiter);
                                        })
                                )
                                .uri("lb://RESEARCH-SERVICE"))

                /*
                 * ==========================================================
                 * PAPER DOWNLOAD
                 * ==========================================================
                 */
                .route("research-download",
                        r -> r.path("/research/papers/*/download")
                                .and()
                                .method(HttpMethod.GET)
                                .filters(f -> f

                                        .circuitBreaker(c -> c
                                                .setName("researchCircuitBreaker")
                                                .setFallbackUri("forward:/fallback/research"))

                                        .requestRateLimiter(config -> {
                                            config.setKeyResolver(userKeyResolver);
                                            config.setRateLimiter(downloadRateLimiter);
                                        })

                                        .retry(retry -> retry
                                                .setRetries(3)
                                                .setMethods(HttpMethod.GET))
                                )
                                .uri("lb://RESEARCH-SERVICE"))

                /*
                 * ==========================================================
                 * PAPER MANAGEMENT
                 * ==========================================================
                 */
                .route("research-paper-management",
                        r -> r.path(
                                        "/research/papers",
                                        "/research/papers/**"
                                )
                                .and()
                                .method(
                                        HttpMethod.POST,
                                        HttpMethod.PUT,
                                        HttpMethod.PATCH,
                                        HttpMethod.DELETE
                                )
                                .filters(f -> f

                                        .circuitBreaker(c -> c
                                                .setName("researchCircuitBreaker")
                                                .setFallbackUri("forward:/fallback/research"))

                                        .requestRateLimiter(config -> {
                                            config.setKeyResolver(userKeyResolver);
                                            config.setRateLimiter(paperRateLimiter);
                                        })
                                )
                                .uri("lb://RESEARCH-SERVICE"))

                /*
                 * ==========================================================
                 * CATEGORY MANAGEMENT
                 * ==========================================================
                 */
                .route("research-category-management",
                        r -> r.path(
                                        "/research/categories",
                                        "/research/categories/**"
                                )
                                .and()
                                .method(
                                        HttpMethod.POST,
                                        HttpMethod.PUT,
                                        HttpMethod.PATCH,
                                        HttpMethod.DELETE
                                )
                                .filters(f -> f

                                        .circuitBreaker(c -> c
                                                .setName("researchCircuitBreaker")
                                                .setFallbackUri("forward:/fallback/research"))

                                        .requestRateLimiter(config -> {
                                            config.setKeyResolver(userKeyResolver);
                                            config.setRateLimiter(categoryRateLimiter);
                                        })
                                )
                                .uri("lb://RESEARCH-SERVICE"))

                /*
                 * ==========================================================
                 * EVERYTHING ELSE
                 * ==========================================================
                 */
                .route("research-service",
                        r -> r.path("/research/**")
                                .filters(f -> f

                                        .circuitBreaker(c -> c
                                                .setName("researchCircuitBreaker")
                                                .setFallbackUri("forward:/fallback/research"))

                                        .retry(retry -> retry
                                                .setRetries(3)
                                                .setMethods(HttpMethod.GET)
                                                .setStatuses(
                                                        HttpStatus.BAD_GATEWAY,
                                                        HttpStatus.GATEWAY_TIMEOUT,
                                                        HttpStatus.SERVICE_UNAVAILABLE))
                                )
                                .uri("lb://RESEARCH-SERVICE"))

                /*
                 * ==========================================================
                 * REVIEW
                 * ==========================================================
                 */
                .route("review-service",
                        r -> r.path("/review/**")
                                .filters(f -> f
                                        .circuitBreaker(c -> c
                                                .setName("reviewCircuitBreaker")
                                                .setFallbackUri("forward:/fallback/review")))
                                .uri("lb://REVIEW-SERVICE"))

                /*
                 * ==========================================================
                 * PLAGIARISM
                 * ==========================================================
                 */
                .route("plagiarism-service",
                        r -> r.path("/plagiarism/**")
                                .filters(f -> f
                                        .circuitBreaker(c -> c
                                                .setName("plagiarismCircuitBreaker")
                                                .setFallbackUri("forward:/fallback/plagiarism")))
                                .uri("lb://PLAGIARISM-SERVICE"))

                /*
                 * ==========================================================
                 * NOTIFICATION
                 * ==========================================================
                 */
                .route("notification-service",
                        r -> r.path(
                                        "/notification/**",
                                        "/test/email"
                                )
                                .filters(f -> f
                                        .circuitBreaker(c -> c
                                                .setName("notificationCircuitBreaker")
                                                .setFallbackUri("forward:/fallback/notification")))
                                .uri("lb://NOTIFICATION-SERVICE"))

                .build();
    }

}