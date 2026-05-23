package product.api.kafka

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import product.api.kafka.event.ProductCreatedEvent

class ProductEventProducer {

    private static final Logger log = LoggerFactory.getLogger(ProductEventProducer)
    private static final String TOPIC = "product-events"

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate

    void sendProductCreated(ProductCreatedEvent event) {
        if (event == null) return

        String jsonEvent = new groovy.json.JsonOutput().toJson(event)
        String key = event.productId?.toString() ?: event.name

        log.info(">>> [PRODUCER] Enviando evento para Kafka - Topic: {}, Key: {}, Payload: {}", TOPIC, key, jsonEvent)

        kafkaTemplate.send(TOPIC, key, jsonEvent)
            .whenComplete { result, ex ->
                if (ex) {
                    log.error(">>> [PRODUCER] FALHA ao enviar mensagem para Kafka", ex)
                } else {
                    log.info(">>> [PRODUCER] Mensagem enviada com SUCESSO para o tópico {}", TOPIC)
                }
            }
    }
}
