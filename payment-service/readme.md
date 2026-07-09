# Initial preparation notes

## Project type
Microservice

## Description
A mock payment processor that simulates payment outcomes (success, failure, timeout).
Exists to enable realistic saga orchestration and rollback testing without integrating
a real payment provider. Stores payment records for auditability.

## Responsibilities
- Accept and process internal payment requests
- Simulate configurable outcomes (success, failure, timeout)
- Persist payment records
- Recognize and handle duplicate payment requests without charging twice

## Non-responsibilities
- Initiating payments or deciding when to charge (Booking Service, as saga orchestrator)
- Routing or exposing endpoints to the client: only reachable internally (Booking Service calls this)
- Orchestrating rollback on failure: returning a failure result is sufficient; rollback is handled by the saga (Booking Service)

## Concerns/constraints
- Must be idempotent: if the saga retries a payment request due to a timeout or failure, the same request must not result in a duplicate charge
- Simulated timeouts must be realistic enough to trigger circuit breaker behavior in the Booking Service (Resilience4j)

## Requirements
- Must accept payment requests from the Booking Service over the internal Docker network
- Must persist each payment record in its own database
- Must return a clear success or failure result for each payment attempt
- Must handle duplicate payment requests idempotently (e.g. via a unique constraint on the payment request identifier)
- Must simulate failure and timeout scenarios to enable saga rollback testing

## Non-requirements
- Does not need to be reachable from the API Gateway or any external client (Booking Service calls this internally)
- Does not need to integrate a real payment provider

## Technologies, tools, and/or frameworks
- Java, Spring Boot
  - Service implementation
- A relational database (Postgres)
  - Persists payment records; connected to via the internal Docker network
  - Idempotency enforced at DB level via unique constraint on payment request identifier

## Dockerizing
Runs as its own container(s) on the internal Docker network. Not publicly exposed: 
reachable only by the Booking Service internally. Can run as multiple instances since
it is stateless (state lives in the database).

## General notes
- Called exclusively by the Booking Service as part of the booking saga
- Its response (success/failure) determines whether the saga confirms or rolls back
- Resilience4j circuit breaker on the Booking Service side wraps calls to this service