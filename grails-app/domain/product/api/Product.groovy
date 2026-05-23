package product.api

import java.time.LocalDateTime

class Product {

    String name
    String description
    BigDecimal price
    LocalDateTime createdAt

    static constraints = {
        name blank: false, nullable: false, maxSize: 255
        description nullable: true, maxSize: 1000
        price nullable: false, min: 0.01G, scale: 2
        createdAt nullable: true
    }

    def beforeInsert() {
        if (!createdAt) {
            createdAt = LocalDateTime.now()
        }
    }

    static mapping = {
        table 'products'
        createdAt column: 'created_at'
        price column: 'price', sqlType: 'numeric(19,2)'
        version false   // optional: remove optimistic locking if not needed
    }

    String toString() {
        name
    }
}
