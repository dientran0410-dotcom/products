package com.fsa.franchise.product_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "mv_pos_menu_catalog")
@Immutable
@Getter
public class PosMenuCatalog {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "franchise_id")
    private UUID franchiseId;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> variants;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allowed_addons", columnDefinition = "jsonb")
    private List<Map<String, Object>> allowedAddons;

    @Column(name = "last_refreshed_at")
    private LocalDateTime lastRefreshedAt;
}
