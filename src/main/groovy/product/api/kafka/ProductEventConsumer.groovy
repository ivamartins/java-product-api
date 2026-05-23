package product.api.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class ProductEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProductEventConsumer)

    @KafkaListener(topics = "product-events", groupId = "product-group")
    void consume(ConsumerRecord<String, Object> record) {
        log.info("=== Kafka Event Received ===")
        log.info("Topic: {}", record.topic())
        log.info("Key: {}", record.key())
        log.info("Value: {}", record.value())
        log.info("Partition: {}, Offset: {}", record.partition(), record.offset())
    }
}
