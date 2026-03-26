package com.fsa.franchise.product_service.service;

import com.fsa.franchise.product_service.dto.request.CategoryCreateRequest;
import com.fsa.franchise.product_service.dto.request.CategoryUpdateRequest;
import com.fsa.franchise.product_service.dto.response.CategoryResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryResponse createCategory(CategoryCreateRequest request);

    CategoryResponse updateCategory(UUID id, CategoryUpdateRequest request);

    void deleteCategory(UUID id);

    List<CategoryResponse> getListCategories(Boolean isTopping, UUID parentId);

    CategoryResponse getCategoryById(UUID id);
}