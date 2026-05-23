package product.api.command

import grails.validation.Validateable

class ProductUpdateCommand implements Validateable {

    String name
    String description
    BigDecimal price

    static constraints = {
        name blank: false, nullable: false, maxSize: 255
        description nullable: true, maxSize: 1000
        price nullable: false, min: 0.01G, scale: 2
    }
}
