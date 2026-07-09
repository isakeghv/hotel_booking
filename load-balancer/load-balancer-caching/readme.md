# Initial preparation notes

## Project type
Caching Layer (Rate Limiting)

## Description
Redis instance dedicated exclusively to backing Traefik's IP-level rate limiting.
Stores and expires request counters per IP address. Has no role in business logic
or any other caching concern in the system.

## Responsibilities
- Store and expire request counters used by Traefik for IP-level rate limiting

## Non-responsibilities
- Per-user/token rate limiting: this is handled by the API Gateway's own Redis instance
- Caching of any business data (availability, idempotency keys, etc.): handled by service-level Redis instances
- Enforcing rate limiting logic itself: that is Traefik's responsibility; this only stores the state

## Concerns/constraints
- If this instance becomes unavailable, Traefik loses rate limiting state: whether Traefik fails open (allows requests) or closed (blocks requests) in this scenario must be decided
- Scoped exclusively to Traefik: no other service should connect to or depend on this instance

## Requirements
- Must be reachable by Traefik on the internal Docker network
- Must support TTL-based expiry on counters

## Non-requirements
- Not required to be accessed by any microservice or the API Gateway (each has its own Redis)
- Not required to persist data across restarts: rate limit counters are ephemeral by nature

## Technologies, tools, and/or frameworks
- Redis
  - In-memory data store used to persist IP-level rate limit counters for Traefik

## Dockerizing
Runs as its own container, defined alongside Traefik in the same project folder.
Should not run as part of the Traefik process itself. Not intended to scale
horizontally: a single instance is sufficient given its narrow, ephemeral purpose.

## General notes
- Strictly scoped to Traefik