# Initial preparation notes

## Project type
Caching layer (Redis): dedicated to Inventory Service

## Description
A Redis instance dedicated exclusively to the Inventory Service.
Caches room availability lookups to reduce load on the Inventory
database. No other service connects to this cache.

## Responsibilities
- Cache room availability data for the Inventory Service
- Serve cached availability lookups to all Inventory Service instances

## Non-responsibilities
- Caching for any other service (each service has its own dedicated Redis)
- Idempotency key storage (booking-redis)
- Distributed locking (booking-redis)

## Concerns/constraints
- Cache invalidation is critical: stale availability data can cause incorrect availability results shown to users
- If this instance goes down, the Inventory Service must fall back to querying the database directly: the system must not crash without cache
- All Inventory Service instances must point to this single Redis instance to ensure consistent cache state across containers

## Requirements
- Must be reachable by all Inventory Service instances via the internal Docker network
- Must support key expiry (TTL) to prevent stale availability data from persisting
- Must support cache-aside pattern (read from cache, fall back to DB on miss, write to cache after DB read)

## Non-requirements
- Does not handle distributed locking (booking-redis)
- Does not handle idempotency keys (booking-redis)
- Does not handle rate limiting (api-gateway-redis)

## Technologies, tools, and/or frameworks
- Redis
- `redis.conf` for configuration (TTL defaults, eviction policy)

## Dockerizing
Runs as its own Docker container on the internal Docker network.
For production, could be set up as a Redis Sentinel cluster (primary + replicas + sentinel)
to avoid a single point of failure, and to allow future scaling without
restructuring the project. Each Inventory Service instance connects to
this cluster via `REDIS_HOST` environment variable defined in `docker-compose.yml`.

## General notes
- Only the Inventory Service should connect to this Redis instance: no other service should reference it
- Eviction policy should be set to `allkeys-lru` (evict least recently used keys when memory is full), appropriate for availability caching