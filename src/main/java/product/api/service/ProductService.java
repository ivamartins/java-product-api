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
 * Importante para estudo:
 *
 * - Métodos de escrita (create/update/delete) **não tocam diretamente** no banco.
 *   Eles apenas publicam eventos no Kafka.
 *   O Consumer é quem chama as stored procedures.
 *
 * - Métodos de leitura (findAll/findById) usam JpaRepository normalmente.
 *   Isso demonstra um padrão comum: leituras diretas + escritas via eventos/procedures.
 */
@Service
public class ProductService {

    private final ProductEventProducer producer;
    private final ProductJpaRepository jpaRepository;

    public ProductService(ProductEventProducer producer, ProductJpaRepository jpaRepository) {
        this.producer = producer;
        this.jpaRepository = jpaRepository;
    }

    // ==================== ESCRITA VIA KAFKA + PL/pgSQL ====================

    @Transactional
    public void createProduct(String name, String description, BigDecimal price) {
        // Publica o evento. O Consumer vai chamar sp_create_product
        producer.sendProductCreated(null, name, description, price);
    }

    @Transactional
    public void updateProduct(Long id, String name, String description, BigDecimal price) {
        producer.sendProductUpdated(id, name, description, price);
    }

    @Transactional
    public void deleteProduct(Long id) {
        producer.sendProductDeleted(id);
    }

    // ==================== LEITURA DIRETA VIA JPA ====================

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
