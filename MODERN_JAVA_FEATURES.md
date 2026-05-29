# Modern Java Features Used in This Project

This document exists for **interview preparation**.

You stopped at Java 8. In interviews, it is very common to be asked:

> "What are the main new features since Java 8 that you have used?"

This file + the comments in the code will help you answer this question confidently and with concrete examples from a real project.

---

## Java Version Used

- **Java 17** (LTS)
- The project is compiled with `release = 17`

---

## Summary of Modern Features Used

| Feature                              | Java Version | Where it is used in this project                  | Interview Talking Point |
|--------------------------------------|--------------|---------------------------------------------------|-------------------------|
| **Records**                          | 14 (stable 16) | `ProductEvent.java`                              | Data carrier classes, immutability, less boilerplate |
| **Switch Expressions**               | 14 (stable 17) | `ProductEventConsumer.java`                      | Cleaner, less error-prone than old switch |
| **Pattern Matching for `instanceof`** | 14 (stable 16) | Can be added in future exercises                 | More readable type checks |
| **`var` keyword** (Local Variable Type Inference) | 10     | Used in several places                           | Reduces verbosity without losing readability |
| **Text Blocks**                      | 15           | Not heavily used yet (good for future exercises) | Multi-line strings |
| **`@PostConstruct`** + modern DI   | -            | `ProductProcedureRepository.java`                | Modern Spring + Java initialization |

---

## 1. Records (Java 14+)

**Old way (Java 8):**
```java
public class ProductEvent {
    private final String eventType;
    private final Long productId;
    // + getters, equals, hashCode, toString, constructor...
}
```

**New way (this project):**
```java
public record ProductEvent(
        String eventType,
        Long productId,
        String name,
        String description,
        BigDecimal price,
        String timestamp
) {
    public static final String EVENT_CREATED = "PRODUCT_CREATED";
    // ...
}
```

**Benefits:**
- Immutable by default
- Auto-generated constructor, getters, `equals()`, `hashCode()`, `toString()`
- Much less boilerplate
- Perfect for DTOs and event objects

**File to study:** `src/main/java/product/api/kafka/event/ProductEvent.java`

---

## 2. Switch Expressions (Java 14+)

**Old way (Java 8):**
```java
switch (eventType) {
    case "PRODUCT_CREATED":
        handleCreate(event);
        break;
    case "PRODUCT_UPDATED":
        handleUpdate(event);
        break;
    default:
        log.warn("Unknown event");
}
```

**New way (this project):**
```java
switch (event.eventType()) {
    case ProductEvent.EVENT_CREATED -> handleCreate(event);
    case ProductEvent.EVENT_UPDATED -> handleUpdate(event);
    case ProductEvent.EVENT_DELETED -> handleDelete(event);
    default -> log.warn("Unknown event type: {}", event.eventType());
}
```

**Benefits:**
- Much cleaner and less error-prone (no `break` needed)
- Can return values directly (expression, not just statement)
- Forces you to handle all cases (or use default)

**File to study:** `src/main/java/product/api/kafka/ProductEventConsumer.java`

---

## 3. `var` Keyword - Local Variable Type Inference (Java 10)

**Old way:**
```java
MapSqlParameterSource params = new MapSqlParameterSource();
List<ProductResponse> products = productService.findAll();
```

**New way (used in this project):**
```java
var params = new MapSqlParameterSource();
var products = productService.findAll();
```

**Benefits:**
- Reduces verbosity
- Still type-safe (compiler infers the type)
- Especially useful with long generic types

**Used in:**
- `ProductProcedureRepository.java`
- `ProductEventConsumer.java`

---

## 4. Other Modern Practices Present

- **Constructor injection** (no `@Autowired` on fields) → Modern Spring recommendation since Spring 4.3 / Java 8+
- **Records + immutable design** → Aligns with modern Java style
- **Clean separation using interfaces + implementation** (JPA Repository + custom Procedure Repository)

---

## How to Talk About This in an Interview

Good answer structure:

> "Since Java 8, I have worked with several important features. In this project, which uses **Java 17**, I actively used:
>
> 1. **Records** — I replaced a traditional DTO with a `record` called `ProductEvent`. This eliminated a lot of boilerplate and made the class immutable by default.
>
> 2. **Switch Expressions** — In the Kafka consumer, I used the new switch expression syntax to route events to different methods. It's much cleaner and eliminates the risk of missing `break` statements.
>
> 3. **`var`** — I used local variable type inference in several places to reduce verbosity while keeping the code readable.
>
> These features helped me write more concise, safer, and modern code."

---

## Recommended Exercises (to practice more modern Java)

1. Refactor one of the DTOs (`ProductCreateRequest`) to use a **Record** (be careful with validation).
2. Use **Pattern Matching for `instanceof`** somewhere in the code.
3. Create a method that uses **Switch Expression with return value**.
4. Use **Text Blocks** to write a multi-line JSON example inside a test or log.

---

## References

- Official Java 17 Release Notes
- JEP 395: Records
- JEP 361: Switch Expressions
- JEP 286: Local-Variable Type Inference (`var`)

---

**Goal of this file:**  
Help you speak confidently in interviews about real usage of modern Java features, with concrete examples from this project.
