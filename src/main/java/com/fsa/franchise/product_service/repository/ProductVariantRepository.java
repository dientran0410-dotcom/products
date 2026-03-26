package com.fsa.franchise.product_service.repository;

import com.fsa.franchise.product_service.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    @Query("SELECT pv FROM ProductVariant pv JOIN FETCH pv.product WHERE pv.id IN :ids")
    List<ProductVariant> findByIdsWithProduct(@Param("ids") List<UUID> ids);

    @Query("SELECT pv FROM ProductVariant pv " +
            "JOIN pv.product addonProd " +
            "JOIN ProductAddon pa ON pa.addonProduct.id = addonProd.id " +
            "WHERE pa.product.id = :productId " +
            "AND pv.status = 'ACTIVE' " +
            "AND addonProd.status = 'ACTIVE'")
    List<ProductVariant> findAddonVariantsByProductId(@Param("productId") UUID productId);

    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId")
    List<ProductVariant> findByProductId(@Param("productId") UUID productId);

}