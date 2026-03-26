package com.fsa.franchise.product_service.dto.response;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

import lombok.Setter;

@Getter
@Setter
@Builder
public class CategoryMenuResponse {
    private UUID categoryId;
    private String categoryName;
    private List<ProductMenuResponse> products;
}
