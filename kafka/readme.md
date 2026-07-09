# Initial preparation notes

## Project type
Message Broker

## Description
Kafka is the async event bus for the booking saga. It decouples services by
allowing them to produce and consume events without direct calls to each other.
Contains no business logic: it only transports and persists events.

## Responsibilities
- Receive events produced by services and deliver them to the correct consumers
- Persist events so they are not lost if a consumer is temporarily unavailable
- Maintain event ordering within each topic/partition

## Non-responsibilities
- Processing, transforming, or acting on event contents (consuming service)
- Enforcing business rules or saga logic (Booking Service)
- Validating event payloads (producing/consuming service)

## Concerns/constraints
- Kafka can redeliver messages: consuming services must handle duplicate delivery (idempotency concern belongs to the consumer, not Kafka)
- If Kafka goes down, the saga stalls: no events can be produced or consumed until it recovers
- Kafka requires either Zookeeper or KRaft for cluster coordination: this choice affects the container setup

## Requirements
- Must support events for each stage of the booking saga (booking, inventory, payment, notification)
- Must persist events for reliability (events survive consumer downtime)
- Must be reachable by all saga participants over the internal Docker network

## Non-requirements
- Does not need to validate or inspect event contents (producing/consuming services)
- Does not need to know about the booking domain or saga logic (Booking Service)

## Technologies, tools, and/or frameworks
- Kafka
  - Core message broker: handles event transport and persistence between saga participants
- Zookeeper (or KRaft)
  - Cluster coordination for Kafka: KRaft is the newer built-in alternative to Zookeeper, removing the need for a separate Zookeeper container

## Dockerizing
Kafka runs as its own container. For fault tolerance, multiple broker instances can
be run behind the same Docker internal network. If using Zookeeper, it requires its
own separate container. If using KRaft, no separate container is needed: coordination
is handled internally by Kafka itself. All instances communicate over the internal
Docker network only; no ports should be publicly exposed.

## General notes
- Producers: Booking Service (saga initiation), Inventory Service, Payment Service
- Consumers: Inventory Service, Payment Service, Notification Service
- Consuming services are responsible for handling duplicate event delivery: Kafka does not guarantee exactly-once delivery by default