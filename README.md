# Guide: Setup and running the project
This project is set up to be run with docker. Install docker if not already installed

## Setup
1. Check
  - [api-gateway readme-file](/api-gateway/api-gateway/README.md)
  - [auth-service readme-file](/auth-service/auth-service-project/README.md)
  - [inventory-service readme-file](/inventory-service/inventory-service-project/README.md)
  - [user-service readme-file](/user-service/user-service-project/README.md)
  - [user-db readme-file](/user-db/README.md)
  - [auth-db readme-file](/auth-db/README.md)
  - [inventory-db readme-file](/inventory-db/README.md)
2. Create .env files with content described in readme files
3. To run project
  - Run from current location
  - Use command `docker compose up --build`
4. Documentation can be found at
  - [Inventory Swagger doc](http://localhost:8080/inventory/swagger-ui/index.html)
  - [Inventory kafka doc](http://localhost:8080/inventory/springwolf/asyncapi-ui.html)