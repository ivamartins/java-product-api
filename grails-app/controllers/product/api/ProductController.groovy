package product.api

import grails.validation.ValidationException
import product.api.command.ProductCreateCommand
import product.api.command.ProductUpdateCommand
import product.api.dto.PagedResult
import static org.springframework.http.HttpStatus.*

class ProductController {

    static responseFormats = ['json']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    ProductService productService

    def index(Integer max, Integer offset) {
        params.max = Math.min(max ?: 10, 100)
        params.offset = offset ?: 0

        def products = productService.list(params)
        def total = Product.count()

        def pagedResult = new PagedResult<>(
            products,
            total,
            (params.offset / params.max) as Integer,
            params.max as Integer
        )

        respond pagedResult
    }

    def show(Long id) {
        def product = productService.get(id)
        if (product == null) {
            render status: NOT_FOUND
            return
        }
        respond product
    }

    def save(ProductCreateCommand cmd) {
        if (cmd.hasErrors()) {
            respond cmd.errors, status: UNPROCESSABLE_ENTITY
            return
        }

        def product = new Product(
            name: cmd.name,
            description: cmd.description,
            price: cmd.price
        )

        try {
            def result = productService.save(product)
            if (result) {
                // Como o salvamento agora é assíncrono via Kafka, retornamos 202 Accepted
                respond result, [status: ACCEPTED]
            } else {
                respond product.errors, status: UNPROCESSABLE_ENTITY
            }
        } catch (ValidationException e) {
            respond product.errors, status: UNPROCESSABLE_ENTITY
        }
    }

    def update(Long id, ProductUpdateCommand cmd) {
        if (cmd.hasErrors()) {
            respond cmd.errors, status: UNPROCESSABLE_ENTITY
            return
        }

        def product = productService.get(id)
        if (product == null) {
            render status: NOT_FOUND
            return
        }

        product.name = cmd.name
        product.description = cmd.description
        product.price = cmd.price

        try {
            def updatedProduct = productService.update(product)
            respond updatedProduct, [status: OK, view: "show"]
        } catch (ValidationException e) {
            respond product.errors, status: UNPROCESSABLE_ENTITY
        }
    }

    def delete(Long id) {
        def product = productService.get(id)
        if (product == null) {
            render status: NOT_FOUND
            return
        }

        try {
            productService.delete(id)
            render status: NO_CONTENT
        } catch (Exception e) {
            render status: INTERNAL_SERVER_ERROR
        }
    }
}
