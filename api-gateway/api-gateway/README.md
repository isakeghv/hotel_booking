# Guide: Setup and running the project

## Setup
Create a .env in current folder, and add:
```
- ACCESS_SECRET=[For verifying access token - same as used in auth-service]
- SPRING_DATA_REDIS_HOST=[gateway's ratelimit cache]
- BLOCKLIST_REDIS_HOST=[gateway's  blocklist cache]
```

## Running
- Run from root of project (position relative to current file: ../../)
- Docker must be installed
- Run, using command `docker compose up --build`

