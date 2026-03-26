package com.fsa.franchise.product_service.repository;

import com.fsa.franchise.product_service.entity.PosMenuCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PosMenuCatalogRepository extends JpaRepository<PosMenuCatalog, UUID> {
    List<PosMenuCatalog> findByFranchiseId(UUID franchiseId);
}