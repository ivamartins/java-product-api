# Study Guide - PL/pgSQL + Kafka + Spring Boot

This guide is the **central reference** for studying this project.  
It focuses on what matters most for your goals:

- **PL/pgSQL Stored Procedures**
- **Kafka event-driven flow**
- **Key Spring Boot annotations and configurations**

---

## 1. Project Architecture Overview

This project follows a **deliberate architectural decision**:

> **All write operations (Create / Update / Delete) go through Kafka and are executed by PL/pgSQL stored procedures.**  
> **Read operations are done directly with Spring Data JPA.**

### Why this design?

- Teaches real-world patterns used in distributed systems.
- Forces you to understand the difference between "intention" (publish event) and "execution" (run procedure).
- Gives you excellent practice with PL/pgSQL, Kafka, and Spring Boot together.

### High-Level Flow

```
HTTP Request
     │
     ▼
ProductController
     │
     ▼
ProductService
     │
     ├── Create/Update/Delete → ProductEventProducer → Kafka Topic
     │
     └── Read (GET) → ProductJpaRepository → Database (via JPA)
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

---

## 2. The Complete Write Flow (Most Important)

This is the core flow you must understand deeply.

### Step-by-Step

1. **HTTP Request arrives** at `ProductController`
   - Example: `POST /api/products`

2. **Controller calls Service**
   - `ProductService.createProduct(...)`

3. **Service publishes an event** (never touches the database directly for writes)
   ```java
   producer.sendProductCreated(null, name, description, price);
   ```

4. **Producer sends message to Kafka**
   - Topic: `product-events`
   - Uses `KafkaTemplate`
   - Message is a JSON representation of `ProductEvent`

5. **Consumer receives the message**
   - Annotated with `@KafkaListener`
   - Deserializes JSON into `ProductEvent`

6. **Consumer routes to the correct procedure**
   ```java
   procedureRepository.createProduct(...);
   ```

7. **ProcedureRepository calls PL/pgSQL**
   - Uses `SimpleJdbcCall` to execute `sp_create_product`
   - The database actually inserts the row

8. **Procedure finishes**
   - Row is created in the `products` table

**Key Learning Point**:  
The Service does **not** know about the database for writes. It only knows how to publish events.

---

## 3. PL/pgSQL - How to View, Study and Modify

### Where the Procedures Live

All PL/pgSQL code is in one file:

```
src/main/resources/db/schema.sql
```

### How Spring Boot Loads the Procedures

In `application.yml`:

```yaml
spring:
  sql:
    init:
      mode: always
      data-locations: classpath:db/schema.sql
```

- Every time the application starts, Spring executes this script.
- This is very convenient while studying.
- In real projects you would use **Flyway** or **Liquibase** instead.

### How to View and Modify Procedures

#### Option A: Using psql (Recommended while studying)

```bash
# Enter the PostgreSQL container
docker exec -it product-db psql -U postgres -d productdb
```

Useful commands inside `psql`:

```sql
-- List all stored procedures
\df sp_*

-- Show the source code of a procedure
\sf sp_create_product

-- Show the source code of a procedure with line numbers
\sf+ sp_create_product

-- Execute a procedure manually (great for testing)
CALL sp_create_product('Test Product', 'Description', 199.90, NULL);

-- See the data
SELECT * FROM products;
```

#### Option B: Editing the file

1. Edit `src/main/resources/db/schema.sql`
2. Restart the Spring Boot application (`./gradlew bootRun`)
3. Spring will re-execute the script (because of `spring.sql.init.mode=always`)

**Warning**: Restarting drops and recreates the procedures. This is good for study, bad for production.

### How to Study PL/pgSQL Effectively

- Always read the procedure source using `\sf` before trying to understand the Java call.
- Modify one procedure at a time and immediately test it.
- Use `RAISE NOTICE` inside procedures to print debug information (visible in PostgreSQL logs).

---

## 4. Kafka - Configuration and Annotations

### Topic and Events

- **Topic name**: `product-events`
- **Events**:
  - `PRODUCT_CREATED`
  - `PRODUCT_UPDATED`
  - `PRODUCT_DELETED`

### Producer Side

**File**: `ProductEventProducer.java`

Key Spring component:

```java
private final KafkaTemplate<String, String> kafkaTemplate;
```

Sending a message:

```java
kafkaTemplate.send(TOPIC, key, json)
```

**Important**: We send `String` (JSON) instead of a typed object for simplicity while studying.

### Consumer Side

**File**: `ProductEventConsumer.java`

Most important annotation:

```java
@KafkaListener(topics = "product-events", groupId = "product-group")
public void consume(ConsumerRecord<String, String> record) { ... }
```

This annotation tells Spring Kafka:
- Listen to the topic `product-events`
- Use consumer group `product-group`
- Automatically deserialize and call this method when a message arrives

### Kafka Configuration

All Kafka settings are in `application.yml`:

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: product-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

**Key properties to understand**:
- `bootstrap-servers`: Where Kafka is running
- `group-id`: Consumer group (important for scaling)
- `auto-offset-reset`: What to do when there is no offset (earliest = read from beginning)

---

## 5. Key Spring Boot Annotations & Configurations

### Core Annotations Used in This Project

| Annotation                    | Where it is used                  | Why it matters |
|-------------------------------|-----------------------------------|----------------|
| `@SpringBootApplication`      | `ProductApiApplication.java`      | Enables auto-configuration and component scanning |
| `@RestController`             | `ProductController.java`          | Marks class as REST controller (combines `@Controller` + `@ResponseBody`) |
| `@RequestMapping`             | `ProductController.java`          | Base path for all endpoints (`/api/products`) |
| `@PostMapping`, `@GetMapping` | `ProductController.java`          | HTTP method mapping |
| `@Valid`                      | Controller methods                | Triggers Bean Validation on DTOs |
| `@Service`                    | `ProductService.java`             | Marks class as Spring-managed service |
| `@Repository`                 | `ProductProcedureRepository.java` | Marks class as data access component |
| `@Component`                  | `ProductEventProducer/Consumer`   | Generic Spring bean |
| `@KafkaListener`              | `ProductEventConsumer.java`       | **Critical** - Declares Kafka consumer method |
| `@Transactional`              | `ProductService.java`             | Ensures methods run inside a transaction |
| `@PostConstruct`              | `ProductProcedureRepository.java` | Runs initialization logic after dependency injection |

### Most Important Configuration File

**Location**: `src/main/resources/application.yml`

Pay special attention to these sections while studying:

- `spring.datasource` → Database connection
- `spring.jpa` → JPA/Hibernate behavior (`ddl-auto: none` is intentional)
- `spring.sql.init` → How procedures are loaded on startup
- `spring.kafka` → All Kafka producer and consumer settings

---

## 6. How to Visualize and Debug the Full Flow

### Recommended Study Workflow

1. Start everything:
   ```bash
   docker compose up -d
   ./gradlew bootRun
   ```

2. Open **three terminals**:
   - Terminal 1: Application logs (`./gradlew bootRun`)
   - Terminal 2: PostgreSQL (`docker exec -it product-db psql ...`)
   - Terminal 3: API calls or `./test-api.sh`

3. Create a product using curl or the test script.

4. Watch what happens in this order:
   - Look for `[PRODUCER]` in the application logs
   - Look for `=== Kafka Event Received ===`
   - Look for `[CONSUMER]` lines
   - In psql, run `SELECT * FROM products;`
   - Run `\sf sp_create_product` to see what was executed

### Useful Logging Patterns

Search the logs for these strings:

- `[PRODUCER]`
- `Kafka Event Received`
- `[CONSUMER]`
- `Procedure sp_`

These were added intentionally to make the flow easy to follow while studying.

---

## 7. Best Practices When Modifying PL/pgSQL

When you edit procedures during your studies:

1. Always use `\sf procedure_name` first to see the current version.
2. Make small changes.
3. Restart the Spring Boot application to reload `schema.sql`.
4. Test immediately with a manual `CALL` in psql.
5. Only after it works in psql, test through the API.

---

## 8. Recommended Study Path

1. Start with **Level 1** of `EXERCISES.md`
2. Read this `STUDY_GUIDE.md` completely at least once
3. Do Exercise 1.1 (Trace the Flow) while reading this guide
4. Move to Level 2 exercises (modifying PL/pgSQL)
5. Only after you are comfortable, go to Level 3 (new Kafka events)

---

## Final Tips

- **Read the procedure source before reading the Java code** that calls it.
- Use `RAISE NOTICE` inside procedures as your main debugging tool.
- The logs in the application are your best friend — read them carefully.
- Change one thing at a time.

This combination of **PL/pgSQL + Kafka + Spring Boot** is very powerful and commonly used in real systems. Mastering the flow explained in this guide will give you a significant advantage.

Good studies!
