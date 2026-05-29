package product.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import product.api.kafka.ProductEventProducer;
import product.api.repository.ProductJpaRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductEventProducer producer;

    @Mock
    private ProductJpaRepository jpaRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(producer, jpaRepository);
    }

    @Test
    @DisplayName("Should publish PRODUCT_CREATED event when creating product with valid data")
    void shouldPublishCreatedEventWhenCreatingProduct() {
        productService.createProduct("Notebook", "16GB", new BigDecimal("4500.00"));

        verify(producer).sendProductCreated(null, "Notebook", "16GB", new BigDecimal("4500.00"));
    }

    @Test
    @DisplayName("Should throw exception when creating product with blank name")
    void shouldThrowExceptionWhenNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                productService.createProduct("   ", "desc", new BigDecimal("100.00"))
        );

        verifyNoInteractions(producer);
    }

    @Test
    @DisplayName("Should publish PRODUCT_UPDATED event")
    void shouldPublishUpdatedEvent() {
        productService.updateProduct(1L, "Updated", "desc", new BigDecimal("200.00"));

        verify(producer).sendProductUpdated(1L, "Updated", "desc", new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("Should publish PRODUCT_DELETED event")
    void shouldPublishDeletedEvent() {
        productService.deleteProduct(99L);

        verify(producer).sendProductDeleted(99L);
    }
}
