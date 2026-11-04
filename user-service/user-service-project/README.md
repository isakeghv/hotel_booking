# Guide: Setup and running the project

## Setup
Create a .env in current folder, and add:
```
- SPRING_DATASOURCE_URL=[url to user-db]
- SPRING_DATASOURCE_USERNAME=[user-db username]
- SPRING_DATASOURCE_PASSWORD=[user-db password]
- SPRING_KAFKA_BOOTSTRAP_SERVERS=[kafka:port]
```

## Running
- Run from root of project (position relative to current file: ../../)
- Docker must be installed
- Run, using command `docker compose up --build`