package com.fsa.franchise.product_service.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class InvoiceItemResponse {

    private UUID productId;
    private Integer quantity;
    private BigDecimal price;

}
