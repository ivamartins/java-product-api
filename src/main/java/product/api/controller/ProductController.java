package product.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import product.api.dto.ProductCreateRequest;
import product.api.dto.ProductResponse;
import product.api.dto.ProductUpdateRequest;
import product.api.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // CUD operations go through Kafka → PL/pgSQL procedures
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody ProductCreateRequest request) {
        productService.createProduct(
                request.getName(),
                request.getDescription(),
                request.getPrice()
        );
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id,
                                       @Valid @RequestBody ProductUpdateRequest request) {
        productService.updateProduct(id, request.getName(), request.getDescription(), request.getPrice());
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.accepted().build();
    }

    // Read operations (direct from DB via JPA)
    @GetMapping
    public ResponseEntity<List<ProductResponse>> findAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
        ProductResponse product = productService.findById(id);
        return product != null ? ResponseEntity.ok(product) : ResponseEntity.notFound().build();
    }
}
