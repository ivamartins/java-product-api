package product.api.kafka

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import product.api.Product

@Component
class ProductEventProducer {

    private static final String TOPIC = "product-events"

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate

    void sendProductCreated(Product product) {
        if (product == null || product.id == null) {
            return
        }

        def event = [
            eventType : "PRODUCT_CREATED",
            productId : product.id,
            name      : product.name,
            description: product.description,
            price     : product.price,
            timestamp : product.createdAt?.toString()
        ]

        kafkaTemplate.send(TOPIC, product.id.toString(), event)
    }
}
