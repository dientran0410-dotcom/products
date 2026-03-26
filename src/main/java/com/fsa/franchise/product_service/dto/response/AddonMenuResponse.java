package com.fsa.franchise.product_service.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
public class AddonMenuResponse {
    private UUID addonVariantId;
    private String addonName;
    private BigDecimal price;
}