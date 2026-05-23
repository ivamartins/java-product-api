package product.api

import grails.gorm.transactions.Transactional
import org.springframework.beans.factory.annotation.Autowired
import product.api.kafka.ProductEventProducer
import product.api.kafka.event.ProductCreatedEvent

@Transactional
class ProductService {

    @Autowired
    ProductEventProducer productEventProducer

    List<Product> list(Map params = [:]) {
        Product.list(params)
    }

    Product get(Long id) {
        Product.get(id)
    }

    // Agora publica o evento no Kafka em vez de salvar direto no banco
    Product save(Product product) {
        def event = new ProductCreatedEvent(
            name: product.name,
            description: product.description,
            price: product.price,
            timestamp: java.time.LocalDateTime.now().toString()
        )

        productEventProducer.sendProductCreated(event)

        // Retornamos um objeto "falso" com os dados (o ID real só existe depois que o consumer salvar)
        return product
    }

    Product update(Product product) {
        product.save(flush: true)
    }

    boolean delete(Long id) {
        Product product = Product.get(id)
        if (product) {
            product.delete(flush: true)
            return true
        }
        return false
    }

    // Chamado pelo Consumer do Kafka (roda em thread separada)
    @Transactional
    Product saveFromKafkaEvent(ProductCreatedEvent event) {
        if (!event) return null

        def product = new Product(
            name: event.name,
            description: event.description,
            price: event.price ? new BigDecimal(event.price.toString()) : null
        )

        if (product.save(flush: true)) {
            log.info(">>> [KAFKA] Produto salvo com sucesso via Kafka: id={}, name={}", product.id, product.name)
            return product
        }

        log.error(">>> [KAFKA] Falha ao salvar produto vindo do Kafka: {}", product.errors)
        return null
    }
}
