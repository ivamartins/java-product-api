// Place your Spring DSL code here
beans = {

    // Registra manualmente o ProductEventProducer para que o ProductService consiga injetá-lo
    productEventProducer(product.api.kafka.ProductEventProducer) { bean ->
        bean.autowire = 'byType'
    }

    // Registra o Consumer explicitamente (para garantir que o @KafkaListener funcione)
    productEventConsumer(product.api.kafka.ProductEventConsumer)

}
