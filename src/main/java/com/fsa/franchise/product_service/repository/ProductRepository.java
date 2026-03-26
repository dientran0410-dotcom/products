package com.fsa.franchise.product_service.repository;

import com.fsa.franchise.product_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> { // Thêm
                                                                                                             // JpaSpecificationExecutor
                                                                                                             // ở đây

    boolean existsByNameAndIdNot(String name, UUID id);

    boolean existsByName(String name);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    boolean existsBySlug(String slug);

    boolean existsByCategoryId(UUID categoryId);

    List<Product> findByStatus(Product.ProductStatus status);

    List<Product> findByStatusAndCategoryIsToppingFalse(Product.ProductStatus status);
}