# Wallet Management — Spring Boot (Sample Project)

This is a minimal but complete scaffold for a **Transaction Wallet Management System** using:

- Spring Boot 3.x
- MongoDB Atlas (spring-boot-starter-data-mongodb) or local Mongo via Docker-compose
- JWT (jjwt) for authentication
- OAuth2 (client + resource server) for third-party login
- Swagger / OpenAPI (springdoc)
- Unit & integration tests with JUnit 5, Mockito and Testcontainers

## Added in this build
- Dockerfile and docker-compose.yml (runs app + local Mongo)
- Atomic debit/credit implemented using Mongo `findAndModify` ($inc) to avoid race conditions
- Refresh tokens and access-token blacklisting saved to Mongo collections
- Integration tests using Testcontainers (MongoDB) demonstrating atomic operations

## How to run with docker-compose (local Mongo)
```bash
# build image locally
mvn clean package -DskipTests
docker-compose up --build
# app available at http://localhost:8080
```

## Run integration tests (requires Docker)
```bash
mvn test -Dskip.unit=false
```

