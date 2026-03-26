package com.fsa.franchise.product_service.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fsa.franchise.product_service.entity.keys.VariantIngredientId;

import java.math.BigDecimal;

@Entity
@Table(name = "variant_ingredients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VariantIngredient {

    @EmbeddedId
    private VariantIngredientId id = new VariantIngredientId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("variantId")
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ingredientId")
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;
}
