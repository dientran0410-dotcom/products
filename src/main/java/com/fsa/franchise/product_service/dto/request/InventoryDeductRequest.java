package com.fsa.franchise.product_service.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class InventoryDeductRequest {
    private BigDecimal quantityToDeduct;
    private String reason;
    private String performedBy;
}