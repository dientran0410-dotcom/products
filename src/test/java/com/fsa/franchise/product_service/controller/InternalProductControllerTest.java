package com.fsa.franchise.product_service.controller;

import com.fsa.franchise.product_service.dto.response.ExternalProductResponse;
import com.fsa.franchise.product_service.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class InternalProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void testGetProducts() throws Exception {
        UUID productId = UUID.randomUUID();
        ExternalProductResponse response = ExternalProductResponse.builder()
                .id(productId)
                .name("Sample Product")
                .price(java.math.BigDecimal.valueOf(15.0))
                .build();

        when(productService.getProducts()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/products/internal/products/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(productId.toString()))
                .andExpect(jsonPath("$[0].name").value("Sample Product"))
                .andExpect(jsonPath("$[0].price").value(15.0));

        verify(productService).getProducts();
    }
}
