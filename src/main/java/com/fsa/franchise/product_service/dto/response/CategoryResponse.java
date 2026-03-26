package com.fsa.franchise.product_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import com.fsa.franchise.product_service.entity.Category.CategoryStatus;

@Getter
@Setter
public class CategoryResponse {
    private UUID id;
    private String name;
    private String slug;
    private UUID parentId;
    private boolean isTopping;
    private CategoryStatus status;
}