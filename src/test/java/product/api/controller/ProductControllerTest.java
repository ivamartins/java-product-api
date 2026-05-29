package product.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import product.api.dto.ProductCreateRequest;
import product.api.service.ProductService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("Should return 202 Accepted when creating a valid product")
    void shouldReturnAcceptedWhenCreatingValidProduct() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("Monitor");
        request.setDescription("27 inch");
        request.setPrice(new BigDecimal("899.90"));

        doNothing().when(productService).createProduct(anyString(), anyString(), any(BigDecimal.class));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(productService).createProduct("Monitor", "27 inch", new BigDecimal("899.90"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when name is missing")
    void shouldReturnBadRequestWhenNameIsMissing() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setDescription("No name");
        request.setPrice(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
