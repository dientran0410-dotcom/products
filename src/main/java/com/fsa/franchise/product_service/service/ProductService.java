package com.fsa.franchise.product_service.service;

import com.fsa.franchise.product_service.dto.request.ProductAvailabilityRequest;
import com.fsa.franchise.product_service.dto.request.ProductCreateRequest;
import com.fsa.franchise.product_service.dto.request.ProductUpdateRequest;
import com.fsa.franchise.product_service.dto.response.ProductAvailabilityResponse;
import com.fsa.franchise.product_service.dto.response.ProductCreateResponse;
import com.fsa.franchise.product_service.dto.response.ProductIngredientsResponse;
import com.fsa.franchise.product_service.dto.response.ProductResponse;
import com.fsa.franchise.product_service.dto.response.VariantMenuResponse;
import com.fsa.franchise.product_service.entity.PosMenuCatalog;

import org.springframework.data.domain.Page; // QUAN TRỌNG: Phải có import này
import org.springframework.data.domain.Pageable; // QUAN TRỌNG: Phải có import này

import java.util.List;
import java.util.UUID;

import com.fsa.franchise.product_service.dto.response.AddonMenuResponse;
import com.fsa.franchise.product_service.dto.response.CategoryMenuResponse;
import com.fsa.franchise.product_service.dto.response.ExternalProductResponse;

public interface ProductService {
    ProductCreateResponse createProduct(ProductCreateRequest request);

    // Lấy danh sách sản phẩm có phân trang và lọc
    Page<ProductResponse> getListProducts(String keyword, UUID categoryId, String status, Pageable pageable);

    ProductResponse getProductDetail(UUID id);

    // Cập nhật thông tin sản phẩm
    ProductResponse updateProduct(UUID id, ProductUpdateRequest request);

    ProductResponse updateProductImage(UUID productId, String imageUrl);

    /**
     * US05: Delete a product
     * Actor: Admin
     * 
     * @param id UUID of the product to delete
     */
    void deleteProduct(UUID id);

    ProductResponse assignCategory(UUID productId, UUID categoryId);

    ProductAvailabilityResponse updateProductAvailability(UUID id, ProductAvailabilityRequest request);

    ProductIngredientsResponse getIngredientsByProduct(UUID productId);

    List<ExternalProductResponse> getProducts();

    List<CategoryMenuResponse> getCategoryMenu();

    List<VariantMenuResponse> getVariantsByProductId(UUID productId);

    List<AddonMenuResponse> getProductAddonsByProductId(UUID productId);

    List<PosMenuCatalog> getPosMenuByFranchise(UUID franchiseId);

    void addProductToFranchiseMenu(UUID franchiseId, UUID productId);
}