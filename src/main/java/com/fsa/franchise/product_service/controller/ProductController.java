package com.fsa.franchise.product_service.controller;

import com.fsa.franchise.product_service.dto.request.ProductAvailabilityRequest;
import com.fsa.franchise.product_service.dto.request.ProductCreateRequest;
import com.fsa.franchise.product_service.dto.request.UpdateImageRequest;
import com.fsa.franchise.product_service.dto.response.ProductCreateResponse;
import com.fsa.franchise.product_service.dto.response.ProductIngredientsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.fsa.franchise.product_service.dto.request.ProductUpdateRequest;
import com.fsa.franchise.product_service.dto.response.AddonMenuResponse;
import com.fsa.franchise.product_service.dto.response.CategoryMenuResponse;
import com.fsa.franchise.product_service.dto.response.ProductAvailabilityResponse;
import com.fsa.franchise.product_service.dto.response.ProductResponse;
import com.fsa.franchise.product_service.dto.response.VariantMenuResponse;
import com.fsa.franchise.product_service.entity.PosMenuCatalog;
import com.fsa.franchise.product_service.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * US01: Get list of products with search, filter and pagination
     * Actor: Admin, Manager
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getListProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductResponse> response = productService.getListProducts(keyword, categoryId, status, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Task 1: Implement API GET /api/products/{id} to return full product details
     * Đây là API lấy chi tiết sản phẩm
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductDetail(@PathVariable UUID id) {
        ProductResponse response = productService.getProductDetail(id);
        return ResponseEntity.ok(response);
    }

    /**
     * US05: Delete product
     * Actor: Admin
     * TODO: Gắn @PreAuthorize("hasRole('ADMIN')") sau khi ráp JWT Security
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable UUID id) {
        // Thực hiện xóa
        productService.deleteProduct(id);

        // Chuẩn bị Response theo đúng API Document
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product deleted successfully");

        // Trả về mã 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductUpdateRequest request) {

        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    // @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductCreateResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {

        ProductCreateResponse response = productService.createProduct(request);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/image")
    public ResponseEntity<ProductResponse> updateProductImage(
            @PathVariable UUID id,
            @RequestBody UpdateImageRequest request) {
        ProductResponse response = productService.updateProductImage(id, request.getImageUrl());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{productId}/category/{categoryId}")
    public ResponseEntity<ProductResponse> assignCategory(
            @PathVariable UUID productId,
            @PathVariable UUID categoryId) {

        ProductResponse response = productService.assignCategory(productId, categoryId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<ProductAvailabilityResponse> updateAvailability(
            @PathVariable UUID id,
            @Valid @RequestBody ProductAvailabilityRequest request) {

        ProductAvailabilityResponse response = productService.updateProductAvailability(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}/ingredients")
    public ProductIngredientsResponse getProductIngredients(@PathVariable UUID productId) {
        return productService.getIngredientsByProduct(productId);
    }

    @GetMapping("/menu")
    public ResponseEntity<List<CategoryMenuResponse>> getCategoryMenu() {
        return ResponseEntity.ok(productService.getCategoryMenu());
    }

    @GetMapping("/{productId}/variants")
    public ResponseEntity<List<VariantMenuResponse>> getProductVariants(@PathVariable UUID productId) {
        return ResponseEntity.ok(productService.getVariantsByProductId(productId));
    }

    @GetMapping("/{productId}/addons")
    public ResponseEntity<List<AddonMenuResponse>> getProductAddons(@PathVariable UUID productId) {
        return ResponseEntity.ok(productService.getProductAddonsByProductId(productId));
    }

    @GetMapping("/menu/franchise")
    public ResponseEntity<List<PosMenuCatalog>> getPosMenuByFranchise(@PathVariable UUID franchiseId) {

        List<PosMenuCatalog> menu = productService.getPosMenuByFranchise(franchiseId);
        return ResponseEntity.ok(menu);
    }

    @PostMapping("/franchise/{franchiseId}/products/{productId}")
    public ResponseEntity<Map<String, String>> addProductToMenu(
            @PathVariable UUID franchiseId,
            @PathVariable UUID productId) {

        productService.addProductToFranchiseMenu(franchiseId, productId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Thêm sản phẩm vào Menu thành công"));
    }
}
