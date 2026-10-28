# Initial preparation notes

## Project type
API Gateway

## Description
Single internal entry point for all client traffic. Responsible for
validating identity, enforcing rate limits, and routing requests to the
correct microservice. Does not contain any business logic.

## Responsibilities
- Validate JWT tokens on every incoming request
- Extract user identity and roles from token and forward downstream as headers
- Enforce per-user/token rate limiting
- Route requests to the correct microservice based on request path
- Reject requests with invalid, expired, or missing tokens
- Coarse-grained authorization (e.g. blocking non-admin roles from admin endpoints)

## Non-responsibilities
- Issuing or refreshing JWTs (Auth Service)
- Business logic or data processing of any kind (respective microservices)

## Concerns/constraints
- Must be stateless: no session or user state stored here, enabling horizontal scaling
- Rate limiting requires Redis: if Redis is unavailable, rate limiting fails
- JWT validation requires the secret/public key to be available at startup

## Requirements
- Must validate JWT signature and expiry on every request before forwarding
- Must forward user ID and roles as headers (e.g. `X-User-Id`, `X-User-Roles`) to downstream services
- Must enforce per-user rate limits backed by Redis
- Must integrate with Eureka for dynamic service discovery and load balancing across microservice instances

## Non-requirements
- Does not need to authenticate users or issue tokens (Auth Service)
- Does not need to perform fine-grained authorization (e.g. "does this user own this booking") (Booking Service)

## Technologies, tools, and/or frameworks
- Java, Spring Boot
- Spring Cloud Gateway
- Spring Cloud LoadBalancer + Eureka Client
- Redis (rate limiting)

## Dockerizing

## General notes
- Sits in front of all microservices: no microservice should be reachable without going through here (except on the internal Docker network for service-to-service calls)
- Auth Service is only called for login/register flows routed through here: JWT validation itself requires no call to Auth Service