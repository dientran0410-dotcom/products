package com.fsa.franchise.product_service.service.serviceImpl;

import com.fsa.franchise.product_service.dto.request.ProductAvailabilityRequest;
import com.fsa.franchise.product_service.dto.response.*;
import com.fsa.franchise.product_service.entity.VariantIngredient;
import com.fsa.franchise.product_service.entity.Product.ProductStatus;
import com.fsa.franchise.product_service.entity.ProductVariant.VariantStatus;
import com.fsa.franchise.product_service.mapper.IngredientMapper;
import com.fsa.franchise.product_service.mapper.ProductDetailMapper;
import com.fsa.franchise.product_service.repository.VariantIngredientRepository;
import lombok.RequiredArgsConstructor;
import com.fsa.franchise.product_service.dto.request.ProductUpdateRequest;
import com.fsa.franchise.product_service.entity.Category;
import com.fsa.franchise.product_service.entity.FranchiseProduct;
import com.fsa.franchise.product_service.entity.Ingredient;
import com.fsa.franchise.product_service.entity.PosMenuCatalog;
import com.fsa.franchise.product_service.entity.Product;
import com.fsa.franchise.product_service.exception.ResourceNotFoundException;
import com.fsa.franchise.product_service.mapper.ProductMapper;
import com.fsa.franchise.product_service.repository.CategoryRepository;
import com.fsa.franchise.product_service.repository.FranchiseProductRepository;
import com.fsa.franchise.product_service.repository.IngredientRepository;
import com.fsa.franchise.product_service.repository.PosMenuCatalogRepository;
import com.fsa.franchise.product_service.repository.ProductRepository;
import com.fsa.franchise.product_service.repository.ProductVariantRepository;
import com.fsa.franchise.product_service.service.ProductService;
import com.fsa.franchise.product_service.specification.ProductSpecification;

import jakarta.persistence.EntityManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fsa.franchise.product_service.dto.request.ProductCreateRequest;
import com.fsa.franchise.product_service.entity.ProductVariant;
import com.fsa.franchise.product_service.exception.DuplicateResourceException;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

        private final ProductRepository productRepository;
        private final CategoryRepository categoryRepository;
        private final ProductMapper productMapper;
        private final ProductDetailMapper productDetailMapper;
        private final VariantIngredientRepository variantIngredientRepository;
        private final IngredientMapper ingredientMapper;
        private final ProductVariantRepository productVariantRepository;
        private final PosMenuCatalogRepository posMenuCatalogRepository;
        private final FranchiseProductRepository franchiseProductRepository;
        private final EntityManager entityManager;
        private final IngredientRepository ingredientRepository;

            @Override
            @Transactional
            public void addProductToFranchiseMenu(UUID franchiseId, UUID productId) {
                    Product product = productRepository.findById(productId)
                                    .orElseThrow(() -> new ResourceNotFoundException(
                                                    "Product not found with id: " + productId));

                    if (franchiseProductRepository.existsByFranchiseIdAndProductId(franchiseId, productId)) {
                            throw new DuplicateResourceException("Product already exists in this franchise menu");
                    }

                    FranchiseProduct mapping = FranchiseProduct.builder()
                                    .franchiseId(franchiseId)
                                    .product(product)
                                    .isAvailable(true)
                                    .build();
                    franchiseProductRepository.save(mapping);

                    entityManager.flush();

                    try {
                            entityManager.createNativeQuery("REFRESH MATERIALIZED VIEW mv_pos_menu_catalog")
                                            .executeUpdate();
                    } catch (Exception e) {
                            System.err.println(
                                            "Cảnh báo: Không thể refresh view, có thể view chưa được update logic JOIN. Lỗi: "
                                                            + e.getMessage());
                    }
            }

        @Override
        @Transactional(readOnly = true)
        public List<PosMenuCatalog> getPosMenuByFranchise(UUID franchiseId) {
                return posMenuCatalogRepository.findByFranchiseId(franchiseId);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<ProductResponse> getListProducts(String keyword, UUID categoryId, String status,
                        Pageable pageable) {
                Specification<Product> spec = ProductSpecification.filterProducts(keyword, categoryId, status);

                Page<Product> productPage = productRepository.findAll(spec, pageable);

                return productPage.map(productMapper::toResponse);
        }

        @Override
        @Transactional(readOnly = true)
        public ProductResponse getProductDetail(UUID id) {
                Product product = productRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

                return productMapper.toResponse(product);
        }

        @Override
        @Transactional
        public ProductResponse updateProduct(UUID id, ProductUpdateRequest request) {
                Product existingProduct = productRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

                if (!existingProduct.getName().equals(request.getName())) {
                        if (productRepository.existsByNameAndIdNot(request.getName(), id)) {
                                throw new RuntimeException("Product name already exists!");
                        }
                        existingProduct.setSlug(generateSlug(request.getName()));
                }

                if (request.getCategoryId() != null
                                && !existingProduct.getCategory().getId().equals(request.getCategoryId())) {
                        Category newCategory = categoryRepository.findById(request.getCategoryId())
                                        .orElseThrow(() -> new RuntimeException("Category not found"));
                        existingProduct.setCategory(newCategory);
                }

                productMapper.updateEntityFromRequest(request, existingProduct);

                Product updatedProduct = productRepository.save(existingProduct);

                // 6. TODO: Call Kafka event để trigger PostgreSQL REFRESH MATERIALIZED VIEW
                // (Cập nhật CQRS read model)

                return productMapper.toResponse(updatedProduct);
        }

        @Override
        @Transactional
        public void deleteProduct(UUID id) {

                Product product = productRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

                product.setDeletedAt(LocalDateTime.now());
                product.setStatus(ProductStatus.INACTIVE);

                productRepository.save(product);
        }

        @Override
        @Transactional
        public ProductResponse assignCategory(UUID productId, UUID categoryId) {
                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Product not found with id: " + productId));

                Category category = categoryRepository.findById(categoryId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Category not found with id: " + categoryId));

                product.setCategory(category);

                return productMapper.toResponse(productRepository.save(product));
        }

        private String generateSlug(String input) {
                String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
                Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
                return pattern.matcher(normalized)
                                .replaceAll("")
                                .toLowerCase()
                                .replaceAll("đ", "d")
                                .replaceAll("[^a-z0-9]+", "-")
                                .replaceAll("^-|-$", "");
        }

        @Override
        @Transactional
        public ProductCreateResponse createProduct(ProductCreateRequest request) {

                Category category = categoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Category not found with id: " + request.getCategoryId()));

                if (productRepository.existsByName(request.getName())) {
                        throw new DuplicateResourceException("Product name already exists!");
                }

                Product product = Product.builder()
                                .name(request.getName())
                                .slug(generateSlug(request.getName()))
                                .description(request.getDescription())
                                .imageUrl(request.getImageUrl())
                                .category(category)
                                .status(ProductStatus.ACTIVE)
                                .build();

                List<ProductVariant> variants = request.getVariants().stream().map(vReq -> {
                        ProductVariant variant = ProductVariant.builder()
                                        .product(product)
                                        .sku(generateSku(category, request.getName(), vReq.getName()))
                                        .name(vReq.getName())
                                        .price(vReq.getPrice())
                                        .isDefault(vReq.isDefault())
                                        .status(VariantStatus.ACTIVE)
                                        .build();
                        return variant;
                }).toList();

                product.setVariants(variants);

                Product savedProduct = productRepository.save(product);

                List<VariantIngredient> variantIngredients = new java.util.ArrayList<>();

                for (int i = 0; i < request.getVariants().size(); i++) {
                        ProductCreateRequest.VariantRequest vReq = request.getVariants().get(i);
                        ProductVariant savedVariant = savedProduct.getVariants().get(i);

                        for (ProductCreateRequest.IngredientRequest ingReq : vReq.getIngredients()) {
                                Ingredient ingredient = ingredientRepository.findById(ingReq.getIngredientId())
                                                .orElseThrow(() -> new ResourceNotFoundException(
                                                                "Ingredient not found with id: "
                                                                                + ingReq.getIngredientId()));

                                VariantIngredient vi = new VariantIngredient();
                                vi.setVariant(savedVariant);
                                vi.setIngredient(ingredient);
                                vi.setQuantity(ingReq.getQuantity());

                                variantIngredients.add(vi);
                        }
                }

                variantIngredientRepository.saveAll(variantIngredients);

                return productDetailMapper.toCreateResponse(savedProduct);
        }

        private String generateSku(Category category, String productName, String variantName) {
                String categoryCode = getInitials(category.getName());
                String productCode = getInitials(productName);
                String sizeCode = mapSizeCode(variantName); // S/M/L

                String uniqueNumber = String.valueOf(System.currentTimeMillis()).substring(10);

                return categoryCode + "-" + productCode + "-" + sizeCode + "-" + uniqueNumber;
        }

        private String getInitials(String input) {
                return Arrays.stream(input.split("\\s+"))
                                .map(word -> word.substring(0, 1).toUpperCase())
                                .collect(Collectors.joining());
        }

        private String mapSizeCode(String variantName) {
                String normalized = variantName.toLowerCase();
                if (normalized.contains("nhỏ"))
                        return "S";
                if (normalized.contains("vừa"))
                        return "M";
                if (normalized.contains("lớn"))
                        return "L";
                return "X"; // fallback neu ko khop
        }

        @Override
        public ProductResponse updateProductImage(UUID productId, String imageUrl) {
                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

                product.setImageUrl(imageUrl);
                product.setUpdatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));

                productRepository.save(product);

                return productMapper.toResponse(product);

        }

        @Override
        @Transactional
        public ProductAvailabilityResponse updateProductAvailability(UUID id, ProductAvailabilityRequest request) {
                Product existingProduct = productRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

                ProductStatus newStatus = request.getIsAvailable() ? ProductStatus.ACTIVE : ProductStatus.INACTIVE;

                existingProduct.setStatus(newStatus);
                productRepository.save(existingProduct);

                return ProductAvailabilityResponse.builder()
                                .id(existingProduct.getId())
                                .isAvailable(request.getIsAvailable())
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public ProductIngredientsResponse getIngredientsByProduct(UUID productId) {

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Product not found with id: " + productId));

                List<VariantDTO> variantDTOs = product.getVariants()
                                .stream()
                                .map(variant -> {

                                        List<VariantIngredient> variantIngredients = variantIngredientRepository
                                                        .findByVariantId(variant.getId());

                                        List<IngredientDTO> ingredientDTOs = variantIngredients
                                                        .stream()
                                                        .map(ingredientMapper::toIngredientDTO)
                                                        .toList();

                                        return VariantDTO.builder()
                                                        .variantId(variant.getId())
                                                        .variantName(variant.getName())
                                                        .sku(variant.getSku())
                                                        .price(variant.getPrice())
                                                        .ingredients(ingredientDTOs)
                                                        .build();
                                })
                                .toList();

                return ProductIngredientsResponse.builder()
                                .productId(product.getId())
                                .productName(product.getName())
                                .variants(variantDTOs)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public List<ExternalProductResponse> getProducts() {
                return productRepository.findAll().stream()
                                .map(this::mapToExternalProductResponse)
                                .collect(Collectors.toList());
        }

        private ExternalProductResponse mapToExternalProductResponse(Product product) {
                java.math.BigDecimal price = (product.getVariants() != null)
                                ? product.getVariants().stream()
                                                .map(v -> v.getPrice())
                                                .filter(java.util.Objects::nonNull)
                                                .min(java.math.BigDecimal::compareTo)
                                                .orElse(java.math.BigDecimal.ZERO)
                                : java.math.BigDecimal.ZERO;
                return ExternalProductResponse.builder()
                                .id(product.getId())
                                .name(product.getName())
                                .price(price)
                                .category(product.getCategory() != null ? product.getCategory().getName()
                                                : "Uncategorized")
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public List<CategoryMenuResponse> getCategoryMenu() {
                List<Product> mainProducts = productRepository
                                .findByStatusAndCategoryIsToppingFalse(Product.ProductStatus.ACTIVE);

                Map<Category, List<Product>> groupedByCategory = mainProducts.stream()
                                .collect(Collectors.groupingBy(Product::getCategory));

                return groupedByCategory.entrySet().stream().map(entry -> {
                        Category category = entry.getKey();
                        List<Product> products = entry.getValue();

                        List<ProductMenuResponse> productDtos = products.stream()
                                        .map(p -> ProductMenuResponse.builder()
                                                        .productId(p.getId())
                                                        .productName(p.getName())
                                                        .build())
                                        .toList();

                        return CategoryMenuResponse.builder()
                                        .categoryId(category.getId())
                                        .categoryName(category.getName())
                                        .products(productDtos)
                                        .build();
                }).toList();
        }

        @Override
        @Transactional(readOnly = true)
        public List<VariantMenuResponse> getVariantsByProductId(UUID productId) {
                List<ProductVariant> variants = productVariantRepository.findByProductId(productId);

                return variants.stream()
                                .filter(v -> v.getStatus() == ProductVariant.VariantStatus.ACTIVE)
                                .map(v -> VariantMenuResponse.builder()
                                                .variantId(v.getId())
                                                .variantName(v.getName() != null && !v.getName().isEmpty() ? v.getName()
                                                                : "Mặc định")
                                                .price(v.getPrice())
                                                .build())
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public List<AddonMenuResponse> getProductAddonsByProductId(UUID productId) {
                List<ProductVariant> addonVariants = productVariantRepository.findAddonVariantsByProductId(productId);

                return addonVariants.stream()
                                .filter(v -> v.getStatus() == ProductVariant.VariantStatus.ACTIVE)
                                .map(v -> {
                                        String fullName = v.getProduct().getName();
                                        if (v.getName() != null && !v.getName().isEmpty() && !v.isDefault()) {
                                                fullName += " (" + v.getName() + ")";
                                        }

                                        return AddonMenuResponse.builder()
                                                        .addonVariantId(v.getId())
                                                        .addonName(fullName)
                                                        .price(v.getPrice())
                                                        .build();
                                })
                                .toList();
        }
}