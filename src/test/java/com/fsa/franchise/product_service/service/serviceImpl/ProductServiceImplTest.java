package com.fsa.franchise.product_service.service.serviceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fsa.franchise.product_service.dto.response.ExternalProductResponse;
import com.fsa.franchise.product_service.entity.Product;
import com.fsa.franchise.product_service.entity.ProductVariant;
import com.fsa.franchise.product_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private UUID productId;
    
    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
    }

    private Product createMockProduct() {
        Product product = new Product();
        product.setId(productId);
        product.setName("Test Product");
        
        ProductVariant variant1 = new ProductVariant();
        variant1.setPrice(BigDecimal.valueOf(10.5));
        
        ProductVariant variant2 = new ProductVariant();
        variant2.setPrice(BigDecimal.valueOf(8.0));
        
        product.setVariants(List.of(variant1, variant2));
        return product;
    }

    @Test
    void testGetProducts() {
        Product product = createMockProduct();
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ExternalProductResponse> result = productService.getProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(productId, result.get(0).getId());
        assertEquals("Test Product", result.get(0).getName());
        assertEquals(BigDecimal.valueOf(8.0), result.get(0).getPrice()); // Should take min price
        verify(productRepository).findAll();
    }
}
