# java-product-api

[![CI](https://github.com/ivamartins/java-product-api/actions/workflows/ci.yml/badge.svg)](https://github.com/ivamartins/java-product-api/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17%2B-blue)](https://openjdk.org/)

> Part of the **Code Solutions Java Modernization Framework** product line. REST API reference with Spring Boot 3, PostgreSQL + PL/pgSQL, and Apache Kafka.

RESTful API for product management using **Spring Boot 3**, **PostgreSQL + PL/pgSQL**, **Apache Kafka**, and **Docker Compose**.

## Why this base

- **Spring Boot 3** with clean architecture (controller / service / repository)
- **PostgreSQL + PL/pgSQL** for stored procedures (common in legacy modernization)
- **Apache Kafka** for event-driven patterns (product events)
- **Docker Compose** for one-command local setup
- **Reference for**: CRUD APIs that need transactional integrity + async events

## Quick start

**Prerequisites:** Java 17+ and Docker.

```bash
# 1) Start Postgres + Kafka
docker compose up -d

# 2) Run the app
./mvnw spring-boot:run
```

The app will start on `http://localhost:8080`.

## API endpoints

- `GET    /api/products` — list all products
- `GET    /api/products/{id}` — get product by id
- `POST   /api/products` — create product
- `PUT    /api/products/{id}` — update product
- `DELETE /api/products/{id}` — delete product

## Run the tests

```bash
./mvnw test
```

## Tech stack

- Java 17+
- Spring Boot 3
- Spring Data JPA
- PostgreSQL + PL/pgSQL
- Apache Kafka
- Docker Compose
- Maven build tool

> **Português?** Veja [`README.pt-BR.md`](./README.pt-BR.md).

## See also

- **Related base**: [quarkus-java-base](https://github.com/ivamartins/quarkus-java-base), [spring-webflux-microservice](https://github.com/ivamartins/spring-webflux-microservice)
- **Product line**: [Java Modernization Framework](https://ivamartins.github.io/code-solutions-site/#produtos)
- **Code Solutions on LinkedIn**: [linkedin.com/company/code-solutions-it](https://www.linkedin.com/company/code-solutions-it/)
- **All Code Solutions open source**: [github.com/ivamartins](https://github.com/ivamartins)

## License

MIT — see `LICENSE`.
