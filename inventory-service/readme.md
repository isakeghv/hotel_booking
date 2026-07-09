# Initial preparation notes

## Project type
Microservice

## Description
Owns all room and availability data. Exposes endpoints for clients to query room
availability, and handles room reservation and release as part of the booking saga.
No other service reads or writes the inventory database directly.

## Responsibilities
- Expose room availability for querying (e.g. available rooms for a given date range)
- Reserve a room when instructed by the Booking Service (saga step)
- Release a room reservation on booking cancellation or saga rollback
- Manage and persist room data
- Cache availability lookups to reduce database load

## Non-responsibilities
- Creating or managing bookings (Booking Service)
- Handling payment of any kind (Payment Service)
- Orchestrating any multi-service flow: only responds to requests, never initiates them

## Concerns/constraints
- Cache must be invalidated immediately when availability changes: stale availability data is a real problem
- Concurrent reservation requests for the same room/date must be handled safely
- Multiple running instances must share the same Redis cache: local in-memory caching is not an option

## Requirements
- Must connect to its own database via the internal Docker network
- Must cache availability data in Redis and invalidate on changes
- Must register with Eureka so the API Gateway and Booking Service can discover it
- Must be horizontally scalable (multiple instances)

## Non-requirements
- Does not need to know about payment or notifications
- Does not need to initiate calls to other services
- Does not need to validate JWTs: identity is resolved by the API Gateway, which forwards user info as headers

## Technologies, tools, and/or frameworks
- Java, Spring Boot
- PostgreSQL database (connected via internal Docker network)
- Redis (availability caching, connected via internal Docker network)
- Eureka Client (service discovery)

## Dockerizing
Runs as one or more Docker containers. Multiple instances are supported and recommended
since availability queries are read-heavy. Eureka handles discovery and load balancing
across instances. Connects to its own dedicated PostgreSQL container and Redis container
via the internal Docker network.

## General notes
- Called externally via the API Gateway (clients querying availability)
- Called internally by the Booking Service during the booking saga (reserve/release)
- Cache invalidation on reservation/release is critical: incorrect availability shown to clients directly impacts bookings