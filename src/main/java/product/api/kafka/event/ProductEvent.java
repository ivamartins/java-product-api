package product.api.kafka.event;

import java.math.BigDecimal;

/**
 * Record (Java 14+).
 * Used here as an immutable event DTO.
 */
public record ProductEvent(
        String eventType,
        Long productId,
        String name,
        String description,
        BigDecimal price,
        String timestamp
) {
    public static final String EVENT_CREATED = "PRODUCT_CREATED";
    public static final String EVENT_UPDATED = "PRODUCT_UPDATED";
    public static final String EVENT_DELETED = "PRODUCT_DELETED";
}
