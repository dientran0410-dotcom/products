package com.fsa.franchise.product_service.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fsa.franchise.product_service.entity.keys.ProductAddonId;

@Entity
@Table(name = "product_addons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductAddon {

    @EmbeddedId
    private ProductAddonId id = new ProductAddonId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("addonProductId")
    @JoinColumn(name = "addon_product_id", nullable = false)
    private Product addonProduct;

    @Column(name = "max_quantity")
    private Integer maxQuantity = 5;
}
