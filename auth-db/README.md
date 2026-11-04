# Guide: Setup and running the project

## Setup
Create a .env in current folder, and add:
```
- POSTGRES_DB=[url to auth-db]
- POSTGRES_PASSWORD=[auth-db username]
- POSTGRES_USER=[auth-db password]
```

## Running
- Run from root of project (position relative to current file: ../)
- Docker must be installed
- Run, using command `docker compose up --build`