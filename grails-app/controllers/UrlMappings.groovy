class UrlMappings {

    static mappings = {
        // REST API for Products under /api/products
        "/api/products"(resources: "product")

        // Default Grails mappings
        "/"(controller: 'application', action:'index')
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
