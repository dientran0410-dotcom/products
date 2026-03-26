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
public class VariantIngredientId implements Serializable {
    @Column(name = "variant_id")
    private UUID variantId;

    @Column(name = "ingredient_id")
    private UUID ingredientId;
}
