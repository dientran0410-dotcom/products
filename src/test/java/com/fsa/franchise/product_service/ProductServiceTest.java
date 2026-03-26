package com.fsa.franchise.product_service;

import com.fsa.franchise.product_service.dto.response.ProductResponse;
import com.fsa.franchise.product_service.entity.Product;
import com.fsa.franchise.product_service.mapper.ProductMapper;
import com.fsa.franchise.product_service.repository.ProductRepository;
import com.fsa.franchise.product_service.service.serviceImpl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Cafe Muối");

        productResponse = new ProductResponse();
        productResponse.setId(product.getId());
        productResponse.setName(product.getName());
    }

    @Test
    @DisplayName("Nên trả về danh sách sản phẩm khi không có bộ lọc")
    void getListProducts_ShouldReturnPageOfProducts() {
        // GIVEN (Giả lập dữ liệu đầu vào)
        Pageable pageable = PageRequest.of(0, 20);
        List<Product> products = List.of(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        // Giả lập hành vi của Repository và Mapper
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        // WHEN (Thực hiện hành động)
        Page<ProductResponse> result = productService.getListProducts(null, null, null, pageable);

        // THEN (Kiểm tra kết quả)
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Cafe Muối", result.getContent().get(0).getName());

        // Xác nhận Repository đã được gọi đúng 1 lần
        verify(productRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Nên trả về trang trống khi không tìm thấy sản phẩm nào")
    void getListProducts_ShouldReturnEmptyPage_WhenNoProductMatches() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> emptyPage = Page.empty(pageable);

        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        // WHEN
        Page<ProductResponse> result = productService.getListProducts("NonExistent", null, "ACTIVE", pageable);

        // THEN
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }
}