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

## Study Guide - What You Should Learn

### 1. Event-Driven Architecture + Stored Procedures

This project implements a powerful pattern:
- Write operations (CUD) **never** go directly to the database from the Service.
- They publish an event to Kafka.
- The Consumer receives the event and calls the Stored Procedure.

**Educational advantages:**
- Clear separation between "intention" and "execution".
- Makes it easy to add complex validations inside the procedure (at the database level).
- Simulates real-world distributed system scenarios.

### 2. Key Files to Study

| File                              | What to Study                                      |
|-----------------------------------|----------------------------------------------------|
| `db/schema.sql`                   | How to write PL/pgSQL procedures                   |
| `ProductProcedureRepository.java` | How to call procedures from Java using `SimpleJdbcCall` |
| `ProductEventConsumer.java`       | How to route events to different procedures        |
| `ProductEventProducer.java`       | How to publish events cleanly                      |
| `ProductService.java`             | How the Service only publishes events (no direct DB access for writes) |

### 3. Recommended Exercises

1. Add an `updated_at` field to the table and update the procedures.
2. Create a trigger that automatically populates `updated_at` on UPDATE.
3. Add minimum price validation inside the `sp_update_product` procedure.
4. Create a new event `PRODUCT_PRICE_CHANGED` and a specific procedure.
5. Try replacing `SimpleJdbcCall` with manual `JdbcTemplate` + `CallableStatement`.

### 4. How to inspect what's happening

```bash
# View application logs (Kafka events + procedure calls)
./gradlew bootRun

# Access PostgreSQL
docker exec -it product-db psql -U postgres -d productdb

# Inside psql:
\df sp_*                    -- list procedures
\sf sp_create_product       -- show procedure source code
TABLE products;             -- view the data
```

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
