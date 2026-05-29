# Study Exercises - Spring Boot + Kafka + PL/pgSQL

This document contains progressive exercises to help you deeply understand the integration between **Spring Boot**, **Apache Kafka**, and **PostgreSQL PL/pgSQL Stored Procedures**.

Work through them in order. Each exercise builds on the previous one.

---

## Level 1: Understanding the Current Architecture

### Exercise 1.1 - Trace the Flow
**Goal:** Understand how a write operation travels through the system.

1. Start the application with `./gradlew bootRun`
2. Create a product using the API:
   ```bash
   curl -X POST http://localhost:8081/api/products \
     -H "Content-Type: application/json" \
     -d '{"name":"Test Product","description":"Exercise","price":99.99}'
   ```
3. Observe the logs. Write down the exact sequence of events:
   - What method in `ProductController` is called?
   - Which method in `ProductService`?
   - What does the `ProductEventProducer` do?
   - What does the `ProductEventConsumer` receive?
   - Which procedure in `ProductProcedureRepository` is executed?

**Deliverable:** Write the complete flow in your own words.

---

### Exercise 1.2 - Read vs Write Paths
**Goal:** Understand the intentional separation between reads and writes.

- Why do `GET` requests use `ProductJpaRepository` directly?
- Why do `POST`/`PUT`/`DELETE` go through Kafka instead of calling the procedures directly from the Service?

**Questions to answer:**
- What are the advantages of this design?
- What are the disadvantages (eventual consistency, complexity, etc.)?

---

## Level 2: Working with PL/pgSQL

### Exercise 2.1 - Add an `updated_at` Column

1. Add a new column `updated_at TIMESTAMP` to the `products` table in `schema.sql`.
2. Modify `sp_update_product` to automatically set `updated_at = NOW()`.
3. Update the `Product` entity to include the new field.
4. Test by updating a product and checking the database.

**Bonus:** Make `updated_at` be set automatically even if someone updates the row directly (not only via the procedure).

---

### Exercise 2.2 - Add Business Validation Inside a Procedure

Modify `sp_create_product` and `sp_update_product` to enforce:

- `price` must be greater than or equal to 1.00
- `name` cannot be empty or only whitespace

If validation fails, raise a proper exception using `RAISE EXCEPTION`.

Test both success and failure cases.

---

### Exercise 2.3 - Create a Trigger

Create a trigger that automatically sets `updated_at = NOW()` whenever a row is updated in the `products` table — even if the update happens outside of `sp_update_product`.

**Requirements:**
- Name the trigger `trg_products_set_updated_at`
- It should fire `BEFORE UPDATE`
- Implement the logic in PL/pgSQL

---

## Level 3: Kafka + Event-Driven Patterns

### Exercise 3.1 - Add a New Event Type: `PRODUCT_PRICE_CHANGED`

1. Add a new constant in `ProductEvent.java`:
   ```java
   public static final String EVENT_PRICE_CHANGED = "PRODUCT_PRICE_CHANGED";
   ```

2. Create a new method in `ProductEventProducer`:
   ```java
   public void sendProductPriceChanged(Long productId, BigDecimal oldPrice, BigDecimal newPrice)
   ```

3. Handle the new event type in `ProductEventConsumer`.

4. Create a new stored procedure `sp_change_product_price(id, new_price)`.

5. Expose a new endpoint:
   ```http
   PATCH /api/products/{id}/price
   ```

**Goal:** Understand how to evolve the event system.

---

### Exercise 3.2 - Add Event Metadata

Enhance the `ProductEvent` record to include:

- `correlationId` (String)
- `source` (e.g. `"product-api"`)

Modify the Producer to generate a UUID for `correlationId`.

Modify the Consumer to log the `correlationId` when processing events.

This is a common pattern in real event-driven systems for tracing.

---

### Exercise 3.3 - Simulate Failure and Recovery

1. Temporarily break the `sp_update_product` procedure (e.g., add a syntax error or make it always raise an exception).
2. Send an update event through the API.
3. Observe what happens in the Consumer.
4. Fix the procedure.
5. Discuss: What strategies could we implement to handle failed events? (Dead Letter Queue, retry, etc.)

---

## Level 4: Advanced / Production-Ready Improvements

### Exercise 4.1 - Replace `SimpleJdbcCall` with Manual `CallableStatement`

Rewrite the methods in `ProductProcedureRepository` using plain `JdbcTemplate` + `CallableStatement` instead of `SimpleJdbcCall`.

Compare the two approaches:
- Which one is more verbose?
- Which one gives you more control?
- When would you choose one over the other?

---

### Exercise 4.2 - Add Outbox Pattern (Optional - Advanced)

Instead of publishing to Kafka directly from the Service, implement a simple version of the **Transactional Outbox** pattern:

1. Create a new table `outbox_events`.
2. When creating/updating/deleting a product, insert a record into `outbox_events` in the **same transaction**.
3. Create a scheduled job (or use Debezium in the future) that reads from `outbox_events` and publishes to Kafka.
4. Mark events as published after successful delivery.

This exercise teaches one of the most important patterns for reliable event publishing.

---

### Exercise 4.3 - Add Integration Tests

Write integration tests that:

- Start Kafka and PostgreSQL using Testcontainers
- Send events through the API
- Assert that the procedures were executed correctly by querying the database
- Verify that the correct Kafka messages were published

---

## Bonus Challenges

1. **Idempotency**: Make the Consumer idempotent. What happens if the same `PRODUCT_CREATED` event is processed twice?
2. **Schema Evolution**: How would you safely evolve a stored procedure signature over time?
3. **Observability**: Add Micrometer metrics around procedure execution time in `ProductProcedureRepository`.
4. **Multi-tenancy**: How would you modify the procedures if we had multiple tenants (e.g., `tenant_id` column)?

---

## How to Approach These Exercises

- Do not rush. The goal is **deep understanding**, not finishing fast.
- After each exercise, write a short note explaining:
  - What you learned
  - What was harder than expected
  - What you would do differently in a real project

Good luck, and enjoy the learning journey!
