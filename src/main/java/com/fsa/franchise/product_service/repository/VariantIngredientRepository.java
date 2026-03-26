package com.fsa.franchise.product_service.repository;

import com.fsa.franchise.product_service.entity.VariantIngredient;
import com.fsa.franchise.product_service.entity.keys.VariantIngredientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VariantIngredientRepository extends JpaRepository<VariantIngredient, VariantIngredientId> {

    // Lấy danh sách nguyên liệu theo variant
    List<VariantIngredient> findByVariantId(UUID variantId);
}
