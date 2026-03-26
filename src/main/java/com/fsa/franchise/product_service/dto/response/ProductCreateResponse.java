package com.fsa.franchise.product_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fsa.franchise.product_service.entity.Product.ProductStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ProductCreateResponse {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private ProductStatus status;

    private CategoryResponse category;
    private List<ProductVariantResponse> variants;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime updatedAt;
}
