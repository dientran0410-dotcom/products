package com.fsa.franchise.product_service.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class VariantMenuResponse {
    private UUID variantId;
    private String variantName;
    private BigDecimal price;
}
