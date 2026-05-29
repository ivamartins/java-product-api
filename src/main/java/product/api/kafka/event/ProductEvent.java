package product.api.kafka.event;

import java.math.BigDecimal;

/**
 * This is a Record (introduced in Java 14, stable since Java 16).
 *
 * Records are a major modern Java feature.
 * They drastically reduce boilerplate for data carrier classes.
 *
 * See MODERN_JAVA_FEATURES.md for a full explanation and interview talking points.
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
