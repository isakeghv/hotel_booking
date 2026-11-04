# Guide: Setup and running the project

## Setup
Create a .env in current folder, and add:
```
- REFRESH_SECRET=[Random hex-string for signing refresh token]
- ACCESS_SECRET=[Random hex-string for signing access token]
- COOKIE_SECURE=[true/false]
- COOKIE_PATH=[cookie path]
- SPRING_DATASOURCE_URL=[url to auth-db]
- SPRING_DATASOURCE_USERNAME=[auth-db username]
- SPRING_DATASOURCE_PASSWORD=[auth-db password]
- SPRING_KAFKA_BOOTSTRAP_SERVERS=[kafka:port]
```

## Running
- Run from root of project (position relative to current file: ../../)
- Docker must be installed
- Run, using command `docker compose up --build`