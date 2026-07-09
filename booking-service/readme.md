# Initial preparation notes

## Project type
Microservice

## Description
Core domain service and saga orchestrator for the booking flow. Handles the full
reservation lifecycle: creating, confirming, and cancelling bookings. Ensures
that a booking and its associated payment can never be triggered more than once,
regardless of retries or double-clicks.

## Responsibilities
- Managing the full booking lifecycle (create, confirm, cancel)
- Orchestrating the booking saga: reserve inventory -> charge payment -> confirm, or roll back on failure
- Enforcing idempotency: ensuring duplicate requests (retries, double-clicks) do not result in duplicate bookings or payments
- Acquiring a distributed lock on (room, date) to prevent two users booking the same room simultaneously

## Non-responsibilities
- Updating room availability directly: this is the responsibility of the Inventory Service
- Processing or validating payment: this is the responsibility of the Payment Service
- Sending booking confirmations: this is the responsibility of the Notification Service

## Concerns/constraints
- Distributed lock TTL must be long enough for the full saga to complete, but short enough to not block the room indefinitely if something fails
- Idempotency keys must have an expiry: needs a conscious decision on how long keys are retained
- Saga rollback logic must be handled explicitly: if payment fails after inventory is reserved, a compensating action must release the room via Inventory Service
- Must only write to its own database: never directly reads or writes Inventory, Payment, or any other service's database

## Requirements
- Must check idempotency key in Redis before starting any saga step
- Must acquire a Redis distributed lock on (room, date) before proceeding with a booking
- Must orchestrate the saga via the message broker (Kafka): not via direct synchronous calls to Payment or Notification
- Must write booking records only to its own database
- Must publish compensating events to roll back saga steps on failure

## Non-requirements
- Does not need to directly call or update the Inventory database (Inventory Service)
- Does not need to implement payment logic (Payment Service)
- Does not need to send notifications (Notification Service)

## Technologies, tools, and/or frameworks
- Java, Spring Boot
- Kafka (saga orchestration)
- Relational database (booking database): Postgres, connected via internal Docker network

## Dockerizing
Runs as one or more Docker containers. Multiple instances are supported: 
Eureka registers each instance, and the API Gateway load balances
across them via Spring Cloud LoadBalancer. Redis and the database are shared
across all instances via the internal Docker network.

## General notes
- Internally calls Inventory Service, Payment Service, and Notification Service via the message broker: the client has no knowledge of these calls
- Is the only service that orchestrates the full booking saga: no other service should initiate or manage this flow
- Does not expose any endpoints that are internal-only: all its REST endpoints are client-facing via the API Gateway