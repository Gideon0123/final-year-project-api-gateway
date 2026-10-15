# API Gateway

The **API Gateway** is the central HTTP entry point for the microservices that make up the **Design and Implementation of a Web-Based Academic Digital Repository and Collaboration System**.

It receives requests from clients, applies gateway-level security and reliability controls, discovers downstream services through Eureka, and forwards each request to the appropriate microservice. The gateway does not implement the business logic of the authentication, research, review, plagiarism, notification, conversation, or collaboration domains. Those domains are implemented in separate services.

Repository: [Gideon0123/final-year-project-api-gateway](https://github.com/Gideon0123/final-year-project-api-gateway)

Related plagiarism service: [Gideon0123/final-year-project-plagiarism-microservice](https://github.com/Gideon0123/final-year-project-plagiarism-microservice)

---

## Table of contents

- [Purpose and scope](#purpose-and-scope)
- [What the gateway includes](#what-the-gateway-includes)
- [What is not included](#what-is-not-included)
- [System architecture](#system-architecture)
- [Technology stack](#technology-stack)
- [Repository structure](#repository-structure)
- [Request lifecycle](#request-lifecycle)
- [Route map](#route-map)
- [Plagiarism service integration](#plagiarism-service-integration)
- [Security](#security)
- [Reliability and resilience](#reliability-and-resilience)
- [Rate limiting](#rate-limiting)
- [Service discovery and load balancing](#service-discovery-and-load-balancing)
- [Configuration](#configuration)
- [Prerequisites](#prerequisites)
- [Running locally](#running-locally)
- [Building and testing](#building-and-testing)
- [Observability and diagnostics](#observability-and-diagnostics)
- [Development notes](#development-notes)
- [Current limitations and future work](#current-limitations-and-future-work)
- [Related services](#related-services)

---

## Purpose and scope

The gateway provides a single entry point for client applications such as a web frontend or other API consumers. Instead of clients connecting directly to every domain microservice, they send requests to the gateway using the public route structure.

The gateway is responsible for cross-cutting concerns that are common to multiple services:

- Routing requests to logical service names.
- Discovering service instances through Eureka.
- Selecting service instances using Spring Cloud LoadBalancer.
- Validating JWT signatures and token expiration.
- Applying role-based authorization rules.
- Forwarding authenticated user identity information to downstream services.
- Applying Redis-backed request rate limiting.
- Protecting downstream calls with circuit breakers.
- Retrying selected failed requests for the research service.
- Forwarding both ordinary HTTP requests and conversation WebSocket traffic.
- Creating and propagating correlation IDs.
- Logging gateway and reactive HTTP activity.
- Returning standard JSON responses for gateway errors and fallback responses.

The gateway is therefore an infrastructure and integration service. Domain-specific operations remain in their respective microservices.

---

## What the gateway includes

### Reactive request routing

The gateway uses Spring Cloud Gateway Server WebFlux to match incoming paths and forward them to downstream services. Most destinations use the `lb://` URI scheme, which means the destination is resolved through service discovery rather than a hard-coded host and port.

### Eureka service discovery

The application registers itself as `API-GATEWAY` and uses Eureka to discover services such as `PLAGIARISM-SERVICE`, `RESEARCH-SERVICE`, and `AUTH-SERVICE`.

### JWT-based security

The gateway validates JWT tokens before allowing protected requests to continue. The JWT implementation uses JJWT. The configured secret is supplied through runtime configuration and is not defined in the committed local properties file.

### Role-based authorization

Spring Security WebFlux is used for authorization. The gateway contains security rules that distinguish between public access, authenticated access, and role-based access.

### Redis rate limiting

Redis is used by the gateway's reactive rate-limiting configuration. Requests to several domain routes pass through a `RedisRateLimiter` using a configured `KeyResolver`.

### Circuit breakers and fallbacks

The gateway defines service-specific circuit breakers. When a downstream service is unavailable or fails according to the circuit-breaker configuration, the request can be forwarded to a service-specific fallback endpoint such as `/fallback/plagiarism`.

### Research-service retries

The general research route retries requests up to three times for configured HTTP methods and selected gateway/service failure statuses:

- `502 Bad Gateway`
- `504 Gateway Timeout`
- `503 Service Unavailable`

### HTTP and WebSocket forwarding

The gateway forwards ordinary HTTP requests as well as the conversation WebSocket route:

```text
/conversation/ws -> lb:ws://CONVERSATION-SERVICE
```

### Correlation and request logging

Correlation IDs are generated or propagated so that a request can be followed across gateway processing and downstream service calls. Logging is enabled for Spring Cloud Gateway, WebFlux, and route definition lookup.

---

## What is not included

The following are **not currently implemented in this repository**:

- Dockerfiles.
- Docker Compose configuration.
- Kubernetes manifests.
- Helm charts.
- Container deployment instructions.
- A database owned by the gateway.
- The implementation of the authentication service.
- The implementation of the research service.
- The implementation of the plagiarism service.
- The implementation of the review service.
- The implementation of the notification service.
- The implementation of the conversation service.
- The implementation of the collaboration service.

Containerization and orchestration may be added in the future, but Docker and Kubernetes should not be treated as part of the current gateway setup.

---

## System architecture

The gateway participates in the wider microservice system as follows:

```text
Client application
       |
       v
API Gateway (API-GATEWAY)
       |
       |-- JWT validation and authorization
       |-- Correlation ID handling
       |-- Rate limiting through Redis
       |-- Circuit breakers and selected retries
       |-- Eureka-based service discovery
       v
Downstream services
       |
       |-- AUTH-SERVICE
       |-- RESEARCH-SERVICE
       |-- REVIEW-SERVICE
       |-- PLAGIARISM-SERVICE
       |-- NOTIFICATION-SERVICE
       |-- CONVERSATION-SERVICE
       |-- COLLABORATION-SERVICE

Supporting infrastructure:

Eureka Server       -> service registration and discovery
Config Server       -> optional external configuration
Redis               -> rate limiting and gateway resilience support
```

The gateway uses logical service identifiers instead of requiring a fixed host and port for every downstream service. For example, the plagiarism route uses `lb://PLAGIARISM-SERVICE`; Eureka resolves that service name to an available instance and Spring Cloud LoadBalancer selects the instance used for the request.

---

## Technology stack

| Technology | Purpose |
|---|---|
| Java 21 | Application language and runtime target |
| Spring Boot 4.0.6 | Application framework and executable service runtime |
| Spring Cloud 2025.1.1 | Cloud-native integration components |
| Spring Cloud Gateway Server WebFlux | Reactive routing and gateway filters |
| Spring Security WebFlux | Authentication and authorization |
| Spring Cloud Netflix Eureka Client | Service registration and discovery |
| Spring Cloud LoadBalancer | Load-balanced service resolution |
| Spring Cloud Config Client | Optional external configuration import |
| Spring Data Redis Reactive | Redis integration for rate limiting and reactive gateway support |
| Resilience4j / Spring Cloud Circuit Breaker | Circuit breakers and fallback handling |
| JJWT 0.12.5 | JWT parsing and validation |
| Apache Commons Pool2 | Connection/resource pooling support |
| Lombok | Boilerplate reduction |
| Maven Wrapper | Reproducible Maven commands without requiring a global Maven installation |

---

## Repository structure

The repository follows a standard Spring Boot Maven layout:

```text
.
├── .mvn/
│   └── wrapper/                       Maven Wrapper support files
├── src/
│   ├── main/
│   │   ├── java/com/example/api_gateway/
│   │   │   ├── ApiGatewayApplication.java
│   │   │   ├── component/             Gateway components and shared application behavior
│   │   │   ├── config/                Routes and application configuration
│   │   │   ├── controller/            Gateway-facing controllers and fallback endpoints
│   │   │   ├── dto/                   Data-transfer objects for gateway responses
│   │   │   ├── exception/              Gateway exception types and handling
│   │   │   ├── filter/                 Request/response and cross-cutting filters
│   │   │   ├── security/               JWT and Spring Security configuration
│   │   │   └── util/                   Shared gateway utilities
│   │   └── resources/
│   │       └── application.properties Application name, discovery, config, logging, and route properties
│   └── test/
│       └── java/com/example/api_gateway/
│           └── ApiGatewayApplicationTests.java
├── pom.xml                            Maven dependencies and build configuration
├── mvnw                                Unix/macOS Maven Wrapper
├── mvnw.cmd                            Windows Maven Wrapper
└── README.md                           Project documentation
```

The main entry point is:

```text
src/main/java/com/example/api_gateway/ApiGatewayApplication.java
```

The programmatic route definitions are located in:

```text
src/main/java/com/example/api_gateway/config/GatewayRoutesConfig.java
```

The committed properties file is located at:

```text
src/main/resources/application.properties
```

---

## Request lifecycle

A typical request follows this flow:

1. A client sends a request to the API Gateway.
2. The gateway assigns or propagates a correlation ID.
3. The request passes through gateway security processing.
4. The JWT is checked when the route requires authentication.
5. Role-based authorization rules are evaluated where configured.
6. The route predicate identifies the destination service.
7. Rate limiting is applied for routes configured with Redis rate limiting.
8. A circuit breaker is applied for routes that define one.
9. Eureka resolves the logical service name to one or more service instances.
10. Spring Cloud LoadBalancer selects a service instance.
11. The gateway forwards the request to the selected instance.
12. The response is returned to the client, or a fallback/error response is produced if the downstream call fails.

For a plagiarism request, the route-level flow is:

```text
Client
  |
  v
API Gateway: /plagiarism/**
  |
  |-- authentication and authorization
  |-- Redis rate limiter
  |-- plagiarism circuit breaker
  v
Eureka: PLAGIARISM-SERVICE
  |
  v
Plagiarism microservice controller
```

---

## Route map

The active route definitions are declared in `GatewayRoutesConfig` unless otherwise noted.

### Authentication and user routes

| Route ID | Incoming paths | Destination | Gateway behavior |
|---|---|---|---|
| `auth-service` | `/auth/**`, `/users/**`, `/admin/users/**` | `lb://AUTH-SERVICE` | Authentication circuit breaker with `/fallback/auth` fallback |

### Research routes

| Route ID | Incoming path | Method | Destination | Gateway behavior |
|---|---|---:|---|---|
| `research-upload` | `/research/papers/*/upload` | `POST` | `lb://RESEARCH-SERVICE` | Dedicated upload route |
| `research-download` | `/research/papers/*/download` | `GET` | `lb://RESEARCH-SERVICE` | Dedicated download route |
| `research-service` | `/research/**` | Any | `lb://RESEARCH-SERVICE` | Redis rate limiting, circuit breaker, and up to three retries for configured failures |

The upload and download routes are defined before the general `/research/**` route, allowing their more specific path and method predicates to be evaluated first.

The `*` in the dedicated research paths represents one path segment. The gateway forwards the request; it does not define the research service's request body or controller contract.

### Review routes

| Route ID | Incoming path | Destination | Gateway behavior |
|---|---|---|---|
| `review-service` | `/review/**` | `lb://REVIEW-SERVICE` | Redis rate limiting and review circuit breaker with `/fallback/review` fallback |

### Plagiarism routes

| Route ID | Incoming path | Destination | Gateway behavior |
|---|---|---|---|
| `plagiarism-service` | `/plagiarism/**` | `lb://PLAGIARISM-SERVICE` | Redis rate limiting and plagiarism circuit breaker with `/fallback/plagiarism` fallback |

The gateway does not rewrite the `/plagiarism` prefix. The downstream plagiarism service also declares controllers under `/plagiarism`, so the forwarded request preserves the service's public API path.

### Notification routes

| Route ID | Incoming paths | Destination | Gateway behavior |
|---|---|---|---|
| `notification-service` | `/notification/**`, `/test/email` | `lb://NOTIFICATION-SERVICE` | Redis rate limiting and notification circuit breaker with `/fallback/notification` fallback |

### Conversation routes

| Route ID | Incoming path | Destination | Gateway behavior |
|---|---|---|---|
| `conversation-service` | `/conversation/ws` | `lb:ws://CONVERSATION-SERVICE` | WebSocket load-balanced forwarding |
| `conversation-service` | `/conversations`, `/conversations/**` | `lb://CONVERSATION-SERVICE` | Redis rate limiting and conversation circuit breaker with `/fallback/conversation` fallback |

### Collaboration routes

| Route ID | Incoming path | Destination | Gateway behavior |
|---|---|---|---|
| `collaboration-service` | `/collaboration/**` | `lb://COLLABORATION-SERVICE` | Redis rate limiting and collaboration circuit breaker with `/fallback/collaboration` fallback |

### Inactive route definitions

The route configuration contains commented-out definitions for some more specific research operations, including:

- Research search routes.
- Research paper-management routes.
- Research category-management routes.

These definitions are inactive and must not be treated as separately available gateway routes. Requests not matched by the active dedicated research routes are handled by the active `/research/**` route.

The commented-out rate-limit, retry, and circuit-breaker blocks for the dedicated research upload and download routes are also inactive.

---

## Plagiarism service integration

The plagiarism microservice is a separate application registered with Eureka as:

```text
PLAGIARISM-SERVICE
```

The API Gateway forwards all requests matching:

```text
/plagiarism/**
```

to:

```text
lb://PLAGIARISM-SERVICE
```

The plagiarism service repository is available at:

[final-year-project-plagiarism-microservice](https://github.com/Gideon0123/final-year-project-plagiarism-microservice)

### Plagiarism service responsibilities

The downstream plagiarism service contains the domain implementation for plagiarism checks. Based on its current source structure, it includes:

- Plagiarism check creation and retrieval.
- Re-running existing plagiarism checks.
- Plagiarism check status tracking.
- Retrieval of the latest check for a paper.
- Retrieval of checks belonging to the current user.
- Administrative listing and deletion of checks.
- Administrative plagiarism statistics.
- Retrieval of individual plagiarism matches.
- Retrieval of paginated matches for a check.
- Research file retrieval and text extraction.
- PDF text extraction through Apache PDFBox.
- DOCX text extraction through Apache POI.
- Research candidate retrieval through Feign clients.
- Research file access through Feign clients.
- Text indexing and similarity-related services.
- Report generation and report download.
- Idempotency handling backed by Redis.
- Security based on gateway-provided authentication information.

### Plagiarism service route contract

The following endpoints are implemented by the downstream plagiarism service. They are available through the gateway's `/plagiarism/**` route when the plagiarism service is registered and reachable.

#### Extract research paper text

```http
GET /plagiarism/extract/{paperId}
```

Allowed roles in the plagiarism service:

- `ADMIN`
- `RESEARCHER`
- `REVIEWER`

The service retrieves the research file for the supplied paper ID and extracts its text.

#### Create a plagiarism check

```http
POST /plagiarism/check
```

Allowed roles:

- `RESEARCHER`
- `ADMIN`

The request body is represented by `CreatePlagiarismCheckRequest`. The operation is marked idempotent with a two-minute idempotency window.

#### Get a plagiarism check

```http
GET /plagiarism/check/{id}
```

Returns the result represented by `PlagiarismCheckResponse`.

#### Get checks for a paper

```http
GET /plagiarism/check/paper/{paperId}
```

Supported query parameters:

| Parameter | Default | Description |
|---|---:|---|
| `page` | `1` | One-based page number supplied by the client |
| `size` | `10` | Number of records per page |
| `sortBy` | `createdAt` | Field used for sorting |
| `sortDirection` | `desc` | Sort direction |

The controller converts the incoming one-based page value to the zero-based page value used internally.

#### Get the latest check for a paper

```http
GET /plagiarism/check/paper/{paperId}/latest
```

Returns the latest plagiarism check associated with the paper.

#### Re-run a plagiarism check

```http
POST /plagiarism/check/{id}/rerun
```

The operation is marked idempotent with a two-minute idempotency window.

#### Get the current user's checks

```http
GET /plagiarism/check/my
```

Supported query parameters:

| Parameter | Default | Description |
|---|---:|---|
| `page` | `1` | One-based page number supplied by the client |
| `size` | `10` | Number of records per page |
| `sortBy` | `createdAt` | Field used for sorting |
| `sortDirection` | `desc` | Sort direction |

#### Get all plagiarism checks

```http
GET /plagiarism/check
```

This endpoint requires the `ADMIN` role.

Supported query parameters:

| Parameter | Default | Description |
|---|---:|---|
| `page` | `1` | One-based page number supplied by the client |
| `size` | `10` | Number of records per page |
| `sortBy` | `createdAt` | Field used for sorting |
| `sortDirection` | `desc` | Sort direction |

#### Delete a plagiarism check

```http
DELETE /plagiarism/check/{id}
```

This endpoint requires the `ADMIN` role and is marked idempotent with a three-minute idempotency window.

#### Get plagiarism statistics

```http
GET /plagiarism/check/statistics
```

This endpoint requires the `ADMIN` role and returns `PlagiarismStatisticsResponse`.

#### Get the status of a check

```http
GET /plagiarism/check/{id}/status
```

Returns `PlagiarismCheckStatusResponse`.

#### Get a paper's plagiarism status

```http
GET /plagiarism/check/paper/{paperId}/status
```

Returns the plagiarism status associated with a paper.

#### Get all matches for a check

```http
GET /plagiarism/match/{id}/matches
```

Supported query parameters:

| Parameter | Default | Description |
|---|---:|---|
| `page` | `1` | One-based page number supplied by the client |
| `size` | `10` | Number of matches per page |
| `sortBy` | `similarityPercentage` | Field used for sorting |
| `sortDirection` | `desc` | Sort direction |

#### Get one plagiarism match

```http
GET /plagiarism/match/{id}
```

Returns one `PlagiarismMatchResponse`.

#### Get a plagiarism report

```http
GET /plagiarism/{id}/report
```

Returns `PlagiarismReportResponse`.

#### Download a plagiarism report

```http
GET /plagiarism/{id}/report/download
```

The plagiarism service returns the report as plain text with a filename in the following format:

```text
plagiarism-report-{id}.txt
```

### Plagiarism service response format

Most plagiarism endpoints return a standard `ApiResponse` wrapper containing fields such as:

- `success`
- `message`
- `status`
- `data`
- `errors`, where applicable
- `path`
- `traceId`
- `timestamp`

Paginated endpoints use a `PagedResponse` inside the `data` field. The pagination response contains fields such as content, page information, first/last indicators, total elements, and total pages.

The report download endpoint is different: it returns a plain-text byte response rather than the JSON response wrapper.

### Plagiarism service internal architecture

The plagiarism service is enabled as a discovery client, Feign client application, JPA-audited application, caching application, asynchronous application, and scheduled application. Its main package areas include:

```text
src/main/java/com/example/PLAGIARISM_SERVICE/
├── component/       Reusable Spring components
├── config/           Cache, Feign, Redis, RabbitMQ, REST, and security configuration
├── controller/       Plagiarism REST controllers
├── dto/              Request and response data-transfer objects
├── entity/           JPA entities and supporting domain objects
├── enums/            Domain enumerations
├── exceptions/       Service-specific exception handling
├── feign/            Research-service Feign clients
├── filter/           Gateway authentication filter
├── mapper/           MapStruct mappings
├── payload/          Pagination and payload wrappers
├── publisher/        Message/event publishing components
├── repository/       Spring Data repositories
├── security/         Principal and authentication error handlers
├── service/          Application service interfaces and implementations
└── utils/            Idempotency and trace ID utilities
```

The gateway README documents this integration for system-level understanding. The plagiarism service README and source code remain the authoritative locations for its implementation details.

---

## Security

### Gateway security responsibilities

The gateway includes Spring Security WebFlux and JWT support. The gateway is intended to validate incoming bearer tokens before forwarding protected requests. JWT configuration, including the secret, must be supplied through the runtime configuration environment.

Do not commit production JWT secrets to this repository.

### Downstream security context

The gateway forwards authenticated user identity information to downstream services through gateway processing. The downstream plagiarism service has its own stateless Spring Security configuration and gateway authentication filter. It permits `/actuator/**` and requires authentication for other requests, with method-level role checks on protected controller methods.

### Roles observed in the plagiarism service

The plagiarism service currently uses the following roles in controller authorization checks:

- `ADMIN`
- `RESEARCHER`
- `REVIEWER`

The effective authorization result depends on the authenticated identity supplied to the service and the role information available in the request context.

### Stateless request handling

The plagiarism service uses stateless session management and disables CSRF for its API security chain. The gateway and downstream service should therefore be treated as token-based APIs rather than session-based browser applications.

---

## Reliability and resilience

The gateway applies resilience mechanisms at the route level.

### Circuit breakers

Active circuit breakers include:

- `authCircuitBreaker`
- `researchCircuitBreaker`
- `reviewCircuitBreaker`
- `plagiarismCircuitBreaker`
- `notificationCircuitBreaker`
- `conversationCircuitBreaker`
- `collaborationCircuitBreaker`

Fallback paths are defined for the services that use circuit breakers:

```text
/fallback/auth
/fallback/research
/fallback/review
/fallback/plagiarism
/fallback/notification
/fallback/conversation
/fallback/collaboration
```

### Retries

The general research route is configured for three retries across configured HTTP methods when the response status indicates a gateway or service availability failure. Retry configuration should be used carefully for operations that may have side effects.

The dedicated research upload and download route retry/filter blocks are currently commented out and therefore inactive.

### Idempotency in the plagiarism service

Idempotency is implemented in the plagiarism service rather than in the gateway route definition. The create, rerun, and administrative delete operations use the service's `@Idempotent` annotation with configured time-to-live values. The plagiarism service includes Redis idempotency configuration and persistence support for this behavior.

---

## Rate limiting

The gateway uses Spring Cloud Gateway's request rate-limiting support with Redis.

Routes using the shared Redis rate limiter include:

- `/research/**`
- `/review/**`
- `/plagiarism/**`
- `/notification/**`
- `/conversations`
- `/conversations/**`
- `/collaboration/**`

The route configuration injects:

- A `KeyResolver`, used to determine the rate-limit key.
- A configured `RedisRateLimiter` instance.

The exact Redis host, port, credentials, and rate values are environment/configuration concerns and are not defined in the committed gateway `application.properties` excerpt.

The dedicated upload and download routes contain commented-out custom rate-limit definitions. Those commented sections are not active.

---

## Service discovery and load balancing

The gateway depends on Eureka for service registration and discovery.

The active Eureka configuration is:

```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
eureka.instance.prefer-ip-address=true
```

The gateway uses logical service names such as:

```text
AUTH-SERVICE
RESEARCH-SERVICE
REVIEW-SERVICE
PLAGIARISM-SERVICE
NOTIFICATION-SERVICE
CONVERSATION-SERVICE
COLLABORATION-SERVICE
```

HTTP destinations use the following form:

```text
lb://SERVICE-NAME
```

The conversation WebSocket destination uses:

```text
lb:ws://CONVERSATION-SERVICE
```

For the plagiarism service to receive gateway traffic, it must be running, registered with Eureka using the service name `PLAGIARISM-SERVICE`, and reachable by the gateway through the Eureka-discovered instance information.

---

## Configuration

The committed gateway properties include:

```properties
spring.application.name=API-GATEWAY
spring.config.import=optional:configserver:http://localhost:8888

eureka.instance.prefer-ip-address=true
eureka.client.service-url.defaultZone=http://localhost:8761/eureka

logging.level.org.springframework.cloud.gateway=TRACE
logging.level.org.springframework.web.reactive=TRACE
logging.level.org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator=TRACE
```

### Application name

```properties
spring.application.name=API-GATEWAY
```

This is the service identity used by the application and by the discovery ecosystem.

### Config Server

```properties
spring.config.import=optional:configserver:http://localhost:8888
```

The Config Server import is optional. If a Config Server is available at `http://localhost:8888`, the gateway can import external configuration from it. If it is not available, the application can continue with locally available configuration, although required runtime values still need to be supplied.

### Eureka Server

```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
```

The default local Eureka endpoint is `http://localhost:8761/eureka`.

### JWT secret

The gateway's JWT service requires a `jwt.secret` property. The committed local gateway properties do not define that secret. Provide it through the Config Server or another environment-specific configuration source.

The secret should be Base64-encoded according to the JWT service's expectations and must not be committed to source control.

### Redis

Redis is required for complete gateway operation because the active route configuration uses Redis-backed request rate limiting. Redis connection settings are expected to be supplied through the application's active configuration sources.

### Logging

Gateway, WebFlux, and route lookup logging are set to `TRACE` in the committed properties. This is useful while developing and diagnosing route matching, but it can be verbose in a shared or production environment.

---

## Prerequisites

For a complete local environment, install or run the following:

1. Java 21.
2. A reachable Eureka Server, normally at `http://localhost:8761/eureka`.
3. A reachable Redis instance for gateway rate limiting.
4. A JWT secret supplied through runtime configuration.
5. The downstream services required by the routes you want to exercise.
6. An optional Config Server at `http://localhost:8888` if external configuration is being used.
7. A service registered in Eureka as `PLAGIARISM-SERVICE` if testing plagiarism routes.

The gateway itself does not provide or start Eureka, Redis, Config Server, or the downstream microservices.

---

## Running locally

Clone the repository and move into its root directory:

```bash
git clone https://github.com/Gideon0123/final-year-project-api-gateway.git
cd final-year-project-api-gateway
```

### macOS or Linux

```bash
./mvnw spring-boot:run
```

### Windows PowerShell

```powershell
.\mvnw.cmd spring-boot:run
```

The gateway will start as the `API-GATEWAY` application. Its ability to forward requests depends on Eureka, Redis, valid JWT configuration, and the relevant downstream services being available.

### Running with external configuration

Before starting the gateway, make sure the following are available when required:

```text
Config Server: http://localhost:8888
Eureka Server: http://localhost:8761/eureka
Redis: configured through the active Spring configuration
JWT secret: configured through the active Spring configuration
```

The Config Server import is optional, but the gateway's JWT and infrastructure configuration still need to be provided by one of the application's configuration sources.

---

## Building and testing

Build the application with the Maven Wrapper.

### macOS or Linux

```bash
./mvnw clean package
```

### Windows PowerShell

```powershell
.\mvnw.cmd clean package
```

Run the test suite with:

### macOS or Linux

```bash
./mvnw test
```

### Windows PowerShell

```powershell
.\mvnw.cmd test
```

The repository contains a Spring context-load test at:

```text
src/test/java/com/example/api_gateway/ApiGatewayApplicationTests.java
```

A successful context test does not by itself prove that every route can reach its downstream service. Full route behavior depends on service discovery, Redis, JWT configuration, and the required downstream applications.

---

## Observability and diagnostics

### Trace logging

The following logging categories are configured at `TRACE` level:

```properties
logging.level.org.springframework.cloud.gateway=TRACE
logging.level.org.springframework.web.reactive=TRACE
logging.level.org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator=TRACE
```

These logs can help diagnose:

- Whether a route predicate matched.
- Which route ID was selected.
- How route definitions were loaded.
- Whether a request reached the reactive gateway chain.
- How a downstream URI was resolved.

### Correlation IDs

The gateway includes correlation ID handling so requests can be traced through the gateway and downstream services. The plagiarism service also includes trace ID values in its standard JSON response wrapper.

### Actuator

The project includes the Spring Boot Actuator dependency. Actuator exposure and gateway endpoint access are represented in the project configuration, but the committed properties file does not expose every actuator endpoint by default. Use the active configuration to determine which actuator endpoints are available in a given environment.

---

## Development notes

### Adding a new service route

A new route should generally be added to `GatewayRoutesConfig` with:

1. A unique route ID.
2. One or more path predicates.
3. Any method predicates needed to distinguish the operation.
4. The correct `lb://SERVICE-NAME` destination.
5. Rate limiting if the service should be protected from excessive traffic.
6. A circuit breaker and fallback when failure isolation is required.
7. Any special handling needed for WebSocket traffic.

The downstream service must use the same logical service name when registering with Eureka.

### Route ordering

Specific routes should be declared before broad catch-all routes. The research upload and download routes are declared before `/research/**` for this reason.

### Gateway versus service responsibilities

Keep the following boundary in mind:

- The gateway owns routing, shared security, rate limiting, resilience, and request forwarding.
- A domain service owns its controllers, validation, business logic, persistence, domain security rules, and domain responses.
- The gateway should not duplicate plagiarism-check processing or research-file extraction logic.

### API response ownership

Gateway fallback responses are produced by gateway fallback controllers/components. Normal plagiarism responses are produced by the plagiarism service controllers and are forwarded through the gateway without the gateway implementing their domain behavior.

---

## Current limitations and future work

The current repository is focused on the gateway service and its integration with the surrounding microservice system.

Potential future work includes:

- Adding Docker or Docker Compose support.
- Adding Kubernetes deployment manifests or Helm charts.
- Adding deployment-specific configuration profiles.
- Expanding integration tests that run against Eureka, Redis, and downstream services.
- Adding more explicit API documentation and generated OpenAPI documentation.
- Refining actuator exposure for operational environments.
- Adding more gateway-level metrics and tracing integrations.
- Enabling or refining currently commented-out research-specific route definitions when the downstream contracts are finalized.

These are future directions, not current repository capabilities.

---

## Related services

The gateway route configuration references the following logical services:

| Service name | Gateway paths | Responsibility in the wider system |
|---|---|---|
| `AUTH-SERVICE` | `/auth/**`, `/users/**`, `/admin/users/**` | Authentication and user-related operations |
| `RESEARCH-SERVICE` | `/research/**` | Research papers, files, uploads, downloads, and research operations |
| `REVIEW-SERVICE` | `/review/**` | Review-related operations |
| `PLAGIARISM-SERVICE` | `/plagiarism/**` | Plagiarism checks, matches, reports, extraction, and statistics |
| `NOTIFICATION-SERVICE` | `/notification/**`, `/test/email` | Notification and email-related operations |
| `CONVERSATION-SERVICE` | `/conversation/ws`, `/conversations/**` | Conversation HTTP and WebSocket operations |
| `COLLABORATION-SERVICE` | `/collaboration/**` | Collaboration-related operations |

The gateway repository contains the routing integration for these services, not their complete implementations.

---

## License

No license is currently declared in the repository metadata.
