package com.fsa.franchise.product_service.controller;

import com.fsa.franchise.product_service.dto.request.CategoryCreateRequest;
import com.fsa.franchise.product_service.dto.request.CategoryUpdateRequest;
import com.fsa.franchise.product_service.dto.response.CategoryResponse;
import com.fsa.franchise.product_service.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryUpdateRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build(); // Trả về 204 No Content
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryDetail(@PathVariable UUID id) {
        CategoryResponse response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getListCategories(
            @RequestParam(required = false) Boolean isTopping,
            @RequestParam(required = false) UUID parentId) {
        List<CategoryResponse> responses = categoryService.getListCategories(isTopping, parentId);
        return ResponseEntity.ok(responses);
    }
}