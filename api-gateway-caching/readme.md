# Initial preparation notes

## Project type
Caching / Rate Limit Store (Redis)

## Description
Dedicated Redis instance for the API Gateway's rate limiting. Stores and manages
per-user and per-IP rate limit counters. This is infrastructure only: contains
no application code.

## Responsibilities
- Store per-user and per-IP rate limit counters
- Automatically expire counters after their time window elapses
- Serve all API Gateway instances from a single shared store, ensuring rate limits
are enforced consistently regardless of which gateway instance handles the request

## Non-responsibilities
- Inventory availability caching (Inventory Redis)
- Booking idempotency keys and distributed locks (Booking Redis)
- Any business logic or application-level concerns

## Concerns/constraints
- If this instance goes down, the API Gateway loses rate limiting capability: the
gateway should be configured to fail open (allow requests through) rather than
fail closed (block everything), to avoid a Redis outage taking down the entire system
- Data is ephemeral by nature: rate limit counters do not need to persist across restarts
- Must be reachable by all API Gateway container instances on the internal Docker network

## Requirements
- Must be accessible by all API Gateway instances via a consistent hostname on the
internal Docker network

## Non-requirements
- Does not need Redis persistence (AOF/RDB): counter data is intentionally short-lived
- Does not need to be accessible by any service other than the API Gateway

## Technologies, tools, and/or frameworks
- Redis

## Dockerizing
Runs as a single dedicated Docker container. All API Gateway instances connect to
this one container, ensuring consistent rate limit state across instances.

For production, this could be replaced with a Redis Sentinel setup (primary +
replicas + sentinel) to eliminate the single point of failure. The folder structure
already supports this: `sentinel.conf` can be added here without changes elsewhere.

## General notes
- Only the API Gateway connects to this Redis instance: no other service should
reference it
- Rate limit counters are keyed by user ID (from JWT) or IP address, set by the
API Gateway configuration