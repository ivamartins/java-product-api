package product.api.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import product.api.kafka.event.ProductEvent;

import java.time.Instant;

/**
 * =====================================================================
 * ProductEventProducer
 * =====================================================================
 *
 * Responsible for publishing product events to Kafka.
 *
 * Eventos suportados:
 * - PRODUCT_CREATED
 * - PRODUCT_UPDATED
 * - PRODUCT_DELETED
 *
 * Esses eventos são consumidos pelo ProductEventConsumer, que por sua
 * turn calls the stored procedures in the database.
 *
 * Padrão utilizado: Event-Driven Architecture (EDA)
 * - O Service não modifica o banco diretamente para operações de escrita.
 * - It only publishes the intention ("what happened").
 * - The Consumer is the one that actually applies the change in the database via PL/pgSQL.
 */
@Component
public class ProductEventProducer {

    private static final Logger log = LoggerFactory.getLogger(ProductEventProducer.class);
    private static final String TOPIC = "product-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ProductEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendProductCreated(Long productId, String name, String description, java.math.BigDecimal price) {
        ProductEvent event = new ProductEvent(
                ProductEvent.EVENT_CREATED,
                productId,
                name,
                description,
                price,
                Instant.now().toString()
        );
        sendEvent(event);
    }

    public void sendProductUpdated(Long productId, String name, String description, java.math.BigDecimal price) {
        ProductEvent event = new ProductEvent(
                ProductEvent.EVENT_UPDATED,
                productId,
                name,
                description,
                price,
                Instant.now().toString()
        );
        sendEvent(event);
    }

    public void sendProductDeleted(Long productId) {
        ProductEvent event = new ProductEvent(
                ProductEvent.EVENT_DELETED,
                productId,
                null,
                null,
                null,
                Instant.now().toString()
        );
        sendEvent(event);
    }

    private void sendEvent(ProductEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            String key = event.productId() != null ? event.productId().toString() : event.name();

            log.info(">>> [PRODUCER] Publishing event → Topic: {}, Key: {}, Type: {}",
                    TOPIC, key, event.eventType());

            kafkaTemplate.send(TOPIC, key, json)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error(">>> [PRODUCER] FAILED to send event to Kafka", ex);
                        } else {
                            log.info(">>> [PRODUCER] Event successfully sent to Kafka");
                        }
                    });

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize ProductEvent to JSON", e);
        }
    }
}
