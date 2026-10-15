# API Gateway

The API Gateway is the entry point for the microservices that support the
**Design and Implementation of a Web-Based Academic Digital Repository and
Collaboration System**. It receives client requests, applies common security
and reliability controls, discovers downstream services through Eureka, and
forwards requests to the appropriate service.

This repository contains the gateway service. The authentication, research,
review, plagiarism, notification, conversation, and collaboration services are
separate applications and are not implemented here.

## What this project includes

- Reactive HTTP routing with Spring Cloud Gateway WebFlux.
- Eureka service discovery and load-balanced `lb://` forwarding.
- JWT signature and expiration validation.
- Role-based authorization with Spring Security WebFlux.
- Forwarding of authenticated user identity through gateway headers.
- Redis-backed request rate limiting.
- Circuit breakers with service-specific fallback endpoints.
- Research-service retries for selected failure statuses.
- HTTP and WebSocket routing for conversation functionality.
- Correlation ID generation and propagation.
- Request logging.
- Standard JSON responses for gateway errors and fallbacks.
- Spring Boot Actuator dependency and permitted actuator paths.

**Current scope:** Docker and Kubernetes are not included in this project yet.
There are no Dockerfiles, Docker Compose files, Kubernetes manifests, Helm
charts, or container deployment instructions in this repository.

## Architecture

The application is a Spring Boot WebFlux gateway registered as `API-GATEWAY`.
Routes use logical service IDs such as `AUTH-SERVICE` and `RESEARCH-SERVICE`.
Eureka resolves those IDs to service instances and Spring Cloud LoadBalancer
selects an instance for forwarding.

| Component | Responsibility | Current configuration |
|---|---|---|
| API Gateway | Entry point, routing, security, resilience, and cross-cutting filters | This repository |
| Eureka | Service registration and discovery | `http://localhost:8761/eureka` |
| Spring Cloud Config | Optional external configuration | `http://localhost:8888` |
| Redis | Reactive rate limiting and token-blacklist storage | Host is supplied by runtime configuration |
| Downstream services | Domain functionality | Resolved through Eureka service IDs |

The application starts from
`com.example.api_gateway.ApiGatewayApplication`, which enables Spring Boot and
discovery-client support.

## Technology stack

- Java 21
- Spring Boot 4.0.6
- Spring Cloud 2025.1.1
- Spring Cloud Gateway Server WebFlux
- Spring Security WebFlux
- Spring Cloud Netflix Eureka Client
- Spring Cloud LoadBalancer
- Spring Cloud Config Client
- Spring Data Redis Reactive
- Resilience4j reactive circuit-breaker integration
- JJWT 0.12.5
- Lombok
- Maven Wrapper

## Prerequisites

The following are required for complete local operation:

1. Java 21.
2. A reachable Eureka Server, normally at `http://localhost:8761/eureka`.
3. A reachable Redis instance for rate limiting.
4. The downstream services that should receive gateway traffic.
5. A JWT secret supplied through runtime configuration.

The Config Server is optional because the import is marked `optional`, but it
is the configured source for external properties when available. `JwtService`
requires the `jwt.secret` property and the committed local properties file does
not define it. Supply a Base64-encoded secret through the Config Server or
another environment-specific configuration source.

## Running the project

Run from the repository root with the Maven Wrapper:

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### macOS or Linux

```bash
./mvnw spring-boot:run
```

Run the existing test suite with:

```powershell
.\mvnw.cmd test
```

The repository currently contains a Spring context-load test at
`src/test/java/com/example/api_gateway/ApiGatewayApplicationTests.java`.
Complete routing behavior still depends on Eureka, Redis, JWT configuration,
and the downstream services being available.

## Application configuration

The committed `src/main/resources/application.properties` includes:

```properties
spring.application.name=API-GATEWAY
spring.config.import=optional:configserver:http://localhost:8888
eureka.instance.prefer-ip-address=true
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
```

It also enables `TRACE` logging for Spring Cloud Gateway, WebFlux, and route
definition lookup. This is useful for development diagnostics but can be very
verbose.

The properties file contains an authentication route definition. The main
route set is declared programmatically in
`src/main/java/com/example/api_gateway/config/GatewayRoutesConfig.java`.

## Route map

The following routes are active in `GatewayRoutesConfig`. The `lb://` scheme
means the destination is resolved through service discovery.

| Route ID | Incoming path | Methods | Destination | Filters |
|---|---|---|---|---|
| `auth-service` | `/auth/**`, `/users/**`, `/admin/users/**` | Any | `AUTH-SERVICE` | Circuit breaker, `/fallback/auth` |
| `research-upload` | `/research/papers/*/upload` | `POST` | `RESEARCH-SERVICE` | Dedicated upload route |
| `research-download` | `/research/papers/*/download` | `GET` | `RESEARCH-SERVICE` | Dedicated download route |
| `research-service` | `/research/**` | Any | `RESEARCH-SERVICE` | Redis rate limiter, circuit breaker, retry |
| `review-service` | `/review/**` | Any | `REVIEW-SERVICE` | Redis rate limiter, circuit breaker |
| `plagiarism-service` | `/plagiarism/**` | Any | `PLAGIARISM-SERVICE` | Redis rate limiter, circuit breaker |
| `notification-service` | `/notification/**`, `/test/email` | Any | `NOTIFICATION-SERVICE` | Redis rate limiter, circuit breaker |
| `conversation-service` | `/conversation/ws` | Any | `CONVERSATION-SERVICE` | WebSocket forwarding with `lb:ws://` |
| `conversation-service` | `/conversations`, `/conversations/**` | Any | `CONVERSATION-SERVICE` | Redis rate limiter, circuit breaker |
| `collaboration-service` | `/collaboration/**` | Any | `COLLABORATION-SERVICE` | Redis rate limiter, circuit breaker |

The upload and download routes are declared before the general `/research/**`
route, so the specific path and method predicates are evaluated first. The
configured `*` represents one path segment; the gateway does not define the
downstream request body or controller contract.

### Inactive route definitions

The source contains commented-out definitions for research search,
paper-management, and category-management routes. They are not active and
must not be treated as available endpoints. The active catch-all
`/research/**` route handles other research requests.

The commented-out rate-limit, retry, and circuit-breaker blocks in the
dedicated upload and download routes are also inactive.
