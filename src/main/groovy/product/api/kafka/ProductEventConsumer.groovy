package product.api.kafka

import groovy.json.JsonSlurper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import product.api.Product
import product.api.ProductService
import product.api.kafka.event.ProductCreatedEvent

import jakarta.annotation.PostConstruct

@Component
class ProductEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProductEventConsumer)

    private final JsonSlurper jsonSlurper = new JsonSlurper()

    @PostConstruct
    void init() {
        log.info(">>> [CONSUMER] ProductEventConsumer inicializado com sucesso. Aguardando eventos do tópico 'product-events'...")
    }

    @Autowired
    ProductService productService

    @KafkaListener(topics = "product-events", groupId = "product-group")
    void consume(ConsumerRecord<String, Object> record) {
        log.info("=== Kafka Event Received ===")
        log.info("Topic: {}, Key: {}", record.topic(), record.key())

        try {
            def data = jsonSlurper.parseText(record.value().toString())

            if (data.eventType == "PRODUCT_CREATED") {
                // Evita duplicação simples
                if (data.productId && Product.get(data.productId as Long)) {
                    log.info("Product {} already exists, skipping", data.productId)
                    return
                }

                // Converte o Map do JSON em um objeto real de evento
                ProductCreatedEvent event = new ProductCreatedEvent(
                    eventType: data.eventType,
                    productId: data.productId ? data.productId as Long : null,
                    name: data.name,
                    description: data.description,
                    price: data.price,
                    timestamp: data.timestamp
                )

                productService.saveFromKafkaEvent(event)
            }
        } catch (Exception e) {
            log.error("Error processing Kafka event", e)
        }
    }
}
