package product.api

import org.springframework.transaction.annotation.Transactional
import product.api.kafka.ProductEventProducer

@Transactional
class ProductService {

    ProductEventProducer productEventProducer

    List<Product> list(Map params = [:]) {
        Product.list(params)
    }

    Product get(Long id) {
        Product.get(id)
    }

    Product save(Product product) {
        if (product.save(flush: true)) {
            // Publish event using dedicated producer (best practice)
            productEventProducer.sendProductCreated(product)
            return product
        }
        return null
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
}
