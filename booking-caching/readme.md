# Initial preparation notes

## Project type
Caching layer (Redis): dedicated to the Booking Service

## Description
A dedicated Redis instance for the Booking Service. Serves two distinct purposes:
storing idempotency keys to prevent duplicate bookings/payments on retried requests,
and holding short-lived distributed locks on (room, date) to prevent race conditions
between concurrent booking attempts.

## Responsibilities
- Store idempotency keys with a TTL so duplicate requests return a cached result
- Hold short-lived distributed locks on (room, date) during the booking stage
- Serve all Booking Service instances as a shared, consistent cache

## Non-responsibilities
- Caching room availability data (Inventory Service Redis)
- Rate limiting (API Gateway Redis)
- Storing persistent booking data (Booking Service database)

## Concerns/constraints
- If this instance goes down, idempotency and locking both fail: double bookings
  and double payments become possible. Treat as critical infrastructure.
- All Booking Service instances must point to this Redis: never use local
  in-memory cache, as that breaks consistency across instances

## Requirements
- Must support atomic distributed locking
- Must support key TTL (expiry) for both locks and idempotency keys
- Must be reachable by all Booking Service instances via the internal Docker network
- Must be a shared instance: not per Booking Service container

## Non-requirements
- Does not need to persist data beyond TTL: Redis in-memory is sufficient,
  no need for RDB/AOF persistence
- Not responsible for caching any other service's data

## Technologies, tools, and/or frameworks
- Redis
- `redis.conf` for configuration (TTL defaults, memory limits, eviction policy)

## Dockerizing
Runs as its own Docker container on the internal Docker network, reachable by all
Booking Service instances via hostname (for example `booking-redis:6379`).

For production, this could be expanded to a Redis Sentinel setup (primary + replicas)
for high availability.

## General notes
- Only the Booking Service connects to this Redis instance
- Suggested key namespaces: `booking:idempotency:` and `booking:lock:`
- Lock TTL should be short (a few seconds): long enough for the saga step to
  complete, short enough to release if the service crashes mid-saga