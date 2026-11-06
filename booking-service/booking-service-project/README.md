# Guide: Setup and running the project

## Setup
Create a .env in current folder, and add:
```
- SPRING_DATASOURCE_URL=[url to booking-db]
- SPRING_DATASOURCE_USERNAME=[booking-db username]
- SPRING_DATASOURCE_PASSWORD=[booking-db password]
- SPRING_KAFKA_BOOTSTRAP_SERVERS=[kafka:port]
- INVENTORY_BASE_URL=[url-to-inventory-service:port]
- PAYMENT_BASE_URL=[url-to-payment-service:port]
```

## Running
- Run from root of project (position relative to current file: ../../)
- Docker must be installed
- Run, using command `docker compose up --build`