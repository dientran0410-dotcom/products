package com.fsa.franchise.product_service.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderResponseV2 {
    private Long id;
    private Long customerId;
    private BigDecimal amount;
    private String status;

}
