package com.fsa.franchise.product_service.repository;

import com.fsa.franchise.product_service.entity.FranchiseProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FranchiseProductRepository extends JpaRepository<FranchiseProduct, UUID> {
    boolean existsByFranchiseIdAndProductId(UUID franchiseId, UUID productId);
}