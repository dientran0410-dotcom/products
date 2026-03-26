package com.fsa.franchise.product_service.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

import com.fsa.franchise.product_service.entity.Ingredient.IngredientStatus;
import com.fsa.franchise.product_service.entity.Ingredient.Unit;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientDTO {
    private UUID ingredientId;
    private String sku;
    private String name;
    private Unit unit;
    private BigDecimal quantity;
    private IngredientStatus status;
}
