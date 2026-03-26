package com.fsa.franchise.product_service.entity.keys;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProductAddonId implements Serializable {
    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "addon_product_id")
    private UUID addonProductId;
}