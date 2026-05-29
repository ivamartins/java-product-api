package product.api.kafka.event;

import java.math.BigDecimal;

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
