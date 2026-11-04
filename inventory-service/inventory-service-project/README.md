# Guide: Setup and running the project

## Setup
Create a .env in current folder, and add:
```
- SPRING_DATASOURCE_URL=[url to inventory-db]
- SPRING_DATASOURCE_USERNAME=[inventory-db username]
- SPRING_DATASOURCE_PASSWORD=[inventory-db password]
- SPRING_KAFKA_BOOTSTRAP_SERVERS=[kafka:port]
- IMAGE_BASE_URL=[base-url to image-service]
- IMAGE_SERVICE_URL=[base-url to image-service]/upload
- HOTEL_CHECKIN_TIME=[desired checkin time, 24h]
- HOTEL_CHECKOUT_TIME=[desired checkout time, 24h]
```

## Running
- Run from root of project (position relative to current file: ../../)
- Docker must be installed
- Run, using command `docker compose up --build`