package com.fsa.franchise.product_service.dto.response;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductIngredientsResponse {
    private UUID productId;
    private String productName;
    private List<VariantDTO> variants;
}
