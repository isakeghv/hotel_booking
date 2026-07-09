# Initial preparation notes

## Project type
Microservice

## Description
Handles user identity management. Responsible for registration, login, and issuing
JWTs. The only service in the system that handles passwords or user credentials.

## Responsibilities
- Register new users
- Authenticate users (login) and issue signed JWTs on success
- Refresh expired JWTs
- Store and manage user credentials and roles

## Non-responsibilities
- Validating JWTs on incoming requests (API Gateway)
- Making authorization decisions, e.g. "can this user access this resource" (respective microservice)
- Any business logic unrelated to identity

## Concerns/constraints
- Passwords must never be stored in plain text: bcrypt hashing
- JWT secret must be externalized via environment variable, never hardcoded

## Requirements
- Must expose endpoints for registration, login, and token refresh
- Must hash passwords using bcrypt before storing
- Must issue signed JWTs containing user ID and roles
- Must connect to its own dedicated Postgres instance
- Must validate credentials against its own DB on login

## Non-requirements
- Does not validate JWTs on every request: that is handled by the API Gateway
- Does not need to know about any other service or their data

## Technologies, tools, and/or frameworks
- Java, Spring Boot
- Spring Security
- jjwt (JWT issuing and signing)
- Connects to auth database via internal Docker network - uses postgres

## Dockerizing
Runs as its own container. Can run as multiple instances since it is stateless: 
all state lives in the Postgres container. Both the service container(s) and its
Postgres container should be on the internal Docker network only, not publicly exposed.

## General notes
- Only called by the API Gateway for login and registration routes
- No other microservice calls the Auth Service directly: downstream services
  rely solely on the `X-User-Id` and `X-User-Roles` headers forwarded by the gateway