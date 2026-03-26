package com.fsa.franchise.product_service.repository;

import com.fsa.franchise.product_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    Optional<Category> findBySlug(String slug);

    @Query("SELECT c FROM Category c WHERE " +
            "(cast(:isTopping as boolean) IS NULL OR c.isTopping = :isTopping) AND " +
            "(cast(:parentId as uuid) IS NULL OR c.parent.id = :parentId) AND " +
            "c.deletedAt IS NULL " +
            "ORDER BY c.createdAt DESC")
    List<Category> findCategoriesByFilters(
            @Param("isTopping") Boolean isTopping,
            @Param("parentId") UUID parentId);
}