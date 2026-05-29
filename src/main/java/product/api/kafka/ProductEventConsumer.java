package product.api.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import product.api.kafka.event.ProductEvent;
import product.api.repository.ProductProcedureRepository;

/**
 * =====================================================================
 * ProductEventConsumer
 * =====================================================================
 *
 * This is one of the most important classes for study.
 *
 * It listens to Kafka messages and is responsible for executing
 * PL/pgSQL stored procedures based on the received event.
 *
 * Full flow (very important to understand):
 *   REST Request → Controller → Service → Producer → Kafka
 *                              → This Consumer → ProcedureRepository → PL/pgSQL Procedure
 *
 * Key Spring Annotation:
 *   @KafkaListener → Tells Spring to call this method automatically
 *                    when a message arrives in the topic.
 *
 * See the full detailed explanation in STUDY_GUIDE.md → Section 2 and 4.
 */
@Component
public class ProductEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProductEventConsumer.class);
    private static final String TOPIC = "product-events";

    private final ProductProcedureRepository procedureRepository;
    private final ObjectMapper objectMapper;

    public ProductEventConsumer(ProductProcedureRepository procedureRepository, ObjectMapper objectMapper) {
        this.procedureRepository = procedureRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Method automatically called by Spring Kafka when a message arrives
     * uma mensagem no tópico.
     */
    @KafkaListener(topics = TOPIC, groupId = "product-group")
    public void consume(ConsumerRecord<String, String> record) {
        log.info("=== Kafka Event Received ===");
        log.info("Topic: {}, Partition: {}, Offset: {}, Key: {}",
                record.topic(), record.partition(), record.offset(), record.key());

        try {
            // Desserializa o JSON para o objeto ProductEvent
            ProductEvent event = objectMapper.readValue(record.value(), ProductEvent.class);

            // Roteia para o método correto de acordo com o tipo de evento
            switch (event.eventType()) {
                case ProductEvent.EVENT_CREATED -> handleCreate(event);
                case ProductEvent.EVENT_UPDATED -> handleUpdate(event);
                case ProductEvent.EVENT_DELETED -> handleDelete(event);
                default -> log.warn("Unknown event type received: {}", event.eventType());
            }

        } catch (Exception e) {
            log.error("Error processing Kafka event. Payload: {}", record.value(), e);
            // Em produção real você enviaria para um Dead Letter Queue (DLQ)
        }
    }

    private void handleCreate(ProductEvent event) {
        log.info("[CONSUMER] Handling PRODUCT_CREATED → calling sp_create_product for name={}", event.name());

        Long generatedId = procedureRepository.createProduct(
                event.name(),
                event.description(),
                event.price()
        );

        log.info("[CONSUMER] Product successfully created via PL/pgSQL. Generated ID = {}", generatedId);
    }

    private void handleUpdate(ProductEvent event) {
        log.info("[CONSUMER] Handling PRODUCT_UPDATED → calling sp_update_product for id={}", event.productId());

        procedureRepository.updateProduct(
                event.productId(),
                event.name(),
                event.description(),
                event.price()
        );

        log.info("[CONSUMER] Product {} updated successfully via PL/pgSQL", event.productId());
    }

    private void handleDelete(ProductEvent event) {
        log.info("[CONSUMER] Handling PRODUCT_DELETED → calling sp_delete_product for id={}", event.productId());

        procedureRepository.deleteProduct(event.productId());

        log.info("[CONSUMER] Product {} deleted successfully via PL/pgSQL", event.productId());
    }
}
