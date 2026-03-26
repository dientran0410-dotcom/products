package com.fsa.franchise.product_service.mapper;

import com.fsa.franchise.product_service.dto.response.IngredientDTO;
import com.fsa.franchise.product_service.entity.VariantIngredient;
import org.springframework.stereotype.Component;

@Component
public class IngredientMapper {

    public IngredientDTO toIngredientDTO(VariantIngredient vi) {
        return IngredientDTO.builder()
                .ingredientId(vi.getIngredient().getId())
                .sku(vi.getIngredient().getSku())
                .name(vi.getIngredient().getName())
                .unit(vi.getIngredient().getUnit())
                .quantity(vi.getQuantity())
                .status(vi.getIngredient().getStatus())
                .build();
    }
}
