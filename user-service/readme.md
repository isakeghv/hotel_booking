# Initial preparation notes

## Project type
Microservice

## Description
Handles user-information management. Used during registration to register the users basic such as name, phone number etc. Should not be mixed with the auth-service. Has its own db for users, which should have the same user_id as in auth-db. Does not handle user credentials (email/password) only for user-information. Is created to make the system more scalable, while respecting the separation of concerns, if it later would be implemented that user information can be updated/modified. This will initially have few requirements and responsibilities, as its main purpose is to support an architecture possible to scale later, not to currently allow accounts to be disabled/modified etc. 

## Responsibilities
- Hold non-auth user-information
- Used during registration of users

## Non-responsibilities
- Authentication
- Authorization
- Issuing jwt tokens
- Validation
- Any auth-related functionalities/responsibilities

## Concerns/constraints
- If there is a mismatch between the user_id used here, and in auth-service (or other places), there will be issues across microservices

## Requirements
- Must hold first- and last-name
- Must hold phone-number
- Must hold only auth-related content
- Should specify if the users account is active or not (with "account_active")

## Non-requirements
- Authentication
- Authorization

## Technologies, tools, and/or frameworks
- Java, Spring Boot
- Spring Security
- jjwt (JWT issuing and signing)
- Connects to user database via internal Docker network - uses postgres

## Dockerizing
Runs as its own container. Can run as multiple instances since it is stateless: 
all state lives in the Postgres container. Both the service container(s) and its
Postgres container should be on the internal Docker network only, not publicly exposed.

## General notes
- For now, this will likely only contain one endpoint (POST) to create the user and find them by email/name/phone number.
- "account_active" is in the DB of both user-service and auth service, as they serve different purposes
  - For auth-service, check if an active account is being attempted to sign into without having to contact the user-service
  - For the user-service, provide an overview of accounts that are active/inactive, so that it can be applied as a filter when querying the db - only to find users, not related to auth.
- Nothing else currently noteworthy, its main task is to support a project which might later scale and therefore need this service.