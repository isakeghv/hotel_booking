# Guide: Setup and running the project

## Setup
Create a .env in current folder, and add:
```
- POSTGRES_DB=[url to booking-db]
- POSTGRES_PASSWORD=[booking-db username]
- POSTGRES_USER=[booking-db password]
```

## Running
- Run from root of project (position relative to current file: ../)
- Docker must be installed
- Run, using command `docker compose up --build`