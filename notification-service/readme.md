# Initial preparation notes

## Project type
Microservice

## Description
Event-driven service responsible for sending booking confirmation notifications
to users. Triggered by events consumed from Kafka at the final step of the
booking saga. Exposes no REST API and owns no data.

## Responsibilities
- Consume booking confirmation events from Kafka
- Send confirmation notifications to the user (e.g. email or SMS)

## Non-responsibilities
- Storing notification history or delivery records (no database)
- Retrying failed notification delivery at the infrastructure level (Kafka handles redelivery)
- Initiating any part of the booking saga (Booking Service)

## Concerns/constraints
- Sits at the end of the saga: a failure here does not roll back the booking or payment
- Notification delivery (e.g. email/SMS) may depend on an external provider in production; for this project a mock/log implementation is acceptable
- Must be idempotent: Kafka may redeliver the same event; sending duplicate notifications should be handled gracefully

## Requirements
- Must consume booking confirmation events from Kafka
- Must handle duplicate event delivery gracefully (idempotency)
- Must send a notification to the user upon receiving a confirmation event

## Non-requirements
- Does not need to expose any REST endpoints (purely event-driven)
- Does not need a database
- Does not need to integrate with Eureka (not called via HTTP by any service)
- Does not need Redis

## Technologies, tools, and/or frameworks
- Java, Spring Boot
  - Service foundation
- Apache Kafka (consumer)
  - Consumes booking confirmation events from the saga

## Dockerizing
Own container. Can run as multiple instances: Kafka consumer groups handle
distributing events across instances automatically, so no additional configuration
is needed to scale horizontally.

## General notes
- Triggered indirectly by the Booking Service via a Kafka event: no direct service-to-service call