# Product API - Spring Boot + Kafka + PL/pgSQL

RESTful API for product management using **Spring Boot 3**, **PostgreSQL + PL/pgSQL**, **Apache Kafka**, and **Docker Compose**.

This project was migrated from Grails to **pure Spring Boot Java** with the goal of studying:

- Spring Boot + Kafka integration (event-driven architecture)
- Calling **PL/pgSQL Stored Procedures** from Java
- Clear separation between commands (via Kafka) and queries (via JPA)

## Technologies Used

- **Spring Boot 3.4.5** (Java 17)
- **Spring Data JPA** + Hibernate
- **PostgreSQL 16** + **PL/pgSQL** (Stored Procedures)
- **Spring Kafka**
- **Docker Compose**
- **JdbcTemplate + SimpleJdbcCall** (for calling procedures)

## Architecture Concept (Important for Study)

```
REST Controller
      │
      ▼
ProductService
      │
      ├─► CUD (Create/Update/Delete) → ProductEventProducer → Kafka
      │
      └─► Reads (GET) → ProductJpaRepository (JPA)
                              │
                              ▼
                    ProductEventConsumer
                              │
                              ▼
                    ProductProcedureRepository
                              │
                              ▼
                    sp_create_product / sp_update_product / sp_delete_product
                              │
                              ▼
                         PostgreSQL (PL/pgSQL)
```

**Recommended study flow:**
- All Create/Update/Delete operations go through **Kafka** and then call a **Stored Procedure**.
- Reads are done directly via JPA (common pattern in real systems).

## How to Run

### 1. Start the infrastructure

```bash
docker compose up -d
```

### 2. Run the application

```bash
./gradlew bootRun
```

The application starts on port **8081**.

## Running the tests

**Português:**

```bash
./gradlew test
```

Ou forçar execução limpa:

```bash
./gradlew clean test
```

Existem testes unitários básicos confiáveis para Service, Controller e Producer (usam mocks). Alguns testes de Kafka (embedded) e IT com Testcontainers (Postgres) podem ser pulados automaticamente se Docker não estiver disponível — o comando principal ainda retorna sucesso.

**English:**

```bash
./gradlew test
```

Or force a clean run:

```bash
./gradlew clean test
```

There are reliable basic unit tests for Service, Controller and Producer (mock-based). Some Kafka (embedded) and Testcontainers (Postgres) tests may be automatically skipped if Docker is not available — the overall command still succeeds.

See the "Useful Commands" section below for the full list.

## Endpoints

| Method | Endpoint                  | Description                              |
|--------|---------------------------|------------------------------------------|
| POST   | `/api/products`           | Create product (via Kafka + Procedure)   |
| PUT    | `/api/products/{id}`      | Update product (via Kafka + Procedure)   |
| DELETE | `/api/products/{id}`      | Delete product (via Kafka + Procedure)   |
| GET    | `/api/products`           | List all (via JPA)                       |
| GET    | `/api/products/{id}`      | Get by ID (via JPA)                      |

Example of creation:

```json
POST /api/products
{
  "name": "Notebook Dell",
  "description": "16GB RAM",
  "price": 4500.00
}
```

## PL/pgSQL - Stored Procedures (Main Focus for Study)

The procedures are located in:

```
src/main/resources/db/schema.sql
```

Main procedures:

- `sp_create_product(name, description, price, INOUT id)`
- `sp_update_product(id, name, description, price)`
- `sp_delete_product(id)`

### How to study the procedures

1. After starting the project, access PostgreSQL:
   ```bash
   docker exec -it product-db psql -U postgres -d productdb
   ```

2. List the procedures:
   ```sql
   \df sp_*
   ```

3. View the source of a procedure:
   ```sql
   \sf sp_create_product
   ```

## Kafka

- Topic: `product-events`
- Events: `PRODUCT_CREATED`, `PRODUCT_UPDATED`, `PRODUCT_DELETED`

The consumer processes the event and calls the corresponding procedure.

## Main Study Resource (Recommended)

**The best and most complete place to study this project is:**

→ **`STUDY_GUIDE.md`**

This file was created specifically for your focus areas:

- PL/pgSQL Stored Procedures (how to view, modify and study them)
- Kafka integration (Producer, Consumer, configuration)
- Key Spring Boot annotations and configurations

It contains detailed explanations of the full flow and practical instructions on how to inspect and debug everything while studying.

Please read `STUDY_GUIDE.md` as your primary reference.

## Useful Commands

```bash
# Build
./gradlew build

# Run tests
./gradlew test

# Stop containers
docker compose down
```

---

Project migrated for educational purposes focusing on **Spring Boot + Kafka + PL/pgSQL**.
