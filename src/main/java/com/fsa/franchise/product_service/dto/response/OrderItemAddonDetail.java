package com.fsa.franchise.product_service.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder
public class OrderItemAddonDetail {
    private String addonName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}