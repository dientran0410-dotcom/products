package com.fsa.franchise.product_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fsa.franchise.product_service.entity.Product.ProductStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private UUID id;

    private String sku;

    private String name;

    private String slug;

    private String description;

    private BigDecimal price;

    private Integer quantity;

    private UUID categoryId;

    private String categoryName;

    private String imageUrl;

    private ProductStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}