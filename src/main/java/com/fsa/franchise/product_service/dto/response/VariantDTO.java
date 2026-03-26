package com.fsa.franchise.product_service.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantDTO {
    private UUID variantId;
    private String variantName;
    private String sku;
    private BigDecimal price;
    private List<IngredientDTO> ingredients;
}
