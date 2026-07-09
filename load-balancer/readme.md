# Initial preparation notes

## Project type
Load Balancer / Reverse Proxy

## Description
The outermost entry point of the system. Sits between the client and the API Gateway,
acting as a reverse proxy to hide internal infrastructure and a load balancer to
distribute traffic across multiple API Gateway instances. The only publicly exposed
component in the system (unless front-end is implemented).

## Responsibilities
- Reverse proxy all incoming client traffic to the API Gateway
- Load balance traffic across multiple API Gateway instances
- IP-level rate limiting to protect the system from spam and abuse before it enters the system
- SSL termination (if applicable)
- Auto-discover API Gateway instances via Docker

## Non-responsibilities
- Per-user/token rate limiting (API Gateway)
- JWT validation or any form of authentication (API Gateway)
- Routing requests to individual microservices (API Gateway)
- Any business logic of any kind (respective microservices)

## Concerns/constraints
- Only publicly exposed container: if it goes down, the entire system is unreachable
- Requires access to the Docker socket to auto-discover containers dynamically
- IP-level rate limiting here is intentionally broad: fine-grained limiting is handled downstream (API Gateway)

## Requirements
- Must auto-discover and load balance across API Gateway instances via Docker
- Must be the only container with publicly exposed ports
- Must forward the real client IP downstream so the API Gateway can use it for rate limiting
- Must perform IP-level rate limiting before traffic reaches the API Gateway

## Non-requirements
- Does not need awareness of any service other than the API Gateway
- Does not need to handle authentication or authorization of any kind (API Gateway)

## Technologies, tools, and/or frameworks
- Traefik
  - Reverse proxy and load balancer. Config-file driven. Auto-discovers Docker containers via the Docker socket, no manual instance registration needed

## Dockerizing
Single container. Does not need to scale horizontally for this project: it is lightweight and not a bottleneck. Requires access to the Docker socket (`/var/run/docker.sock`) to enable auto-discovery of API Gateway instances.

## General notes
- All other containers must not have publicly exposed ports: internal communication happens exclusively over the Docker internal network