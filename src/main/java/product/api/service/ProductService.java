package product.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import product.api.dto.ProductResponse;
import product.api.entity.Product;
import product.api.kafka.ProductEventProducer;
import product.api.repository.ProductJpaRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * =====================================================================
 * ProductService
 * =====================================================================
 *
 * Camada de serviço da aplicação.
 *
 * Important for study:
 *
 * - Write methods (create/update/delete) do not touch the database directly.
 *   They only publish events to Kafka.
 *   The Consumer is the one that calls the stored procedures.
 *
 * - Read methods (findAll/findById) use JpaRepository normally.
 *   This demonstrates a common pattern: direct reads + writes via events/procedures.
 */
@Service
public class ProductService {

    // Constructor injection (modern Spring recommendation since 4.3)
    private final ProductEventProducer producer;
    private final ProductJpaRepository jpaRepository;

    public ProductService(ProductEventProducer producer, ProductJpaRepository jpaRepository) {
        this.producer = producer;
        this.jpaRepository = jpaRepository;
    }

    // ==================== WRITE OPERATIONS (via Kafka + PL/pgSQL) ====================

    @Transactional
    public void createProduct(String name, String description, BigDecimal price) {
        // Modern Java 11+: String.isBlank() instead of name == null || name.trim().isEmpty()
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }

        // Modern Java 10+: var for local variables
        var eventPrice = price;

        producer.sendProductCreated(null, name, description, eventPrice);
    }

    @Transactional
    public void updateProduct(Long id, String name, String description, BigDecimal price) {
        producer.sendProductUpdated(id, name, description, price);
    }

    @Transactional
    public void deleteProduct(Long id) {
        producer.sendProductDeleted(id);
    }

    // ==================== READ OPERATIONS (direct via JPA) ====================

    public List<ProductResponse> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse findById(Long id) {
        return jpaRepository.findById(id)
                .map(this::toResponse)
                .orElse(null);
    }

    private ProductResponse toResponse(Product p) {
        ProductResponse dto = new ProductResponse();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setPrice(p.getPrice());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }
}
