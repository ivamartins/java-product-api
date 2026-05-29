package product.api.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductEventProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ProductEventProducer producer;

    @Captor
    private ArgumentCaptor<String> valueCaptor;

    @BeforeEach
    void setUp() {
        // Provide a real ObjectMapper (required by the producer)
        producer = new ProductEventProducer(kafkaTemplate, new com.fasterxml.jackson.databind.ObjectMapper());

        when(kafkaTemplate.send(any(), any(), any()))
                .thenReturn(new CompletableFuture<>());
    }

    @Test
    @DisplayName("Should send PRODUCT_CREATED event to the correct topic")
    void shouldSendCreatedEvent() {
        producer.sendProductCreated(null, "Headset", null, new BigDecimal("350.00"));

        verify(kafkaTemplate).send(eq("product-events"), any(), valueCaptor.capture());

        String payload = valueCaptor.getValue();
        assertThat(payload).contains("PRODUCT_CREATED");
        assertThat(payload).contains("Headset");
    }

    @Test
    @DisplayName("Should send PRODUCT_DELETED event")
    void shouldSendDeletedEvent() {
        producer.sendProductDeleted(77L);

        verify(kafkaTemplate).send(eq("product-events"), any(), valueCaptor.capture());

        assertThat(valueCaptor.getValue()).contains("PRODUCT_DELETED");
    }
}
