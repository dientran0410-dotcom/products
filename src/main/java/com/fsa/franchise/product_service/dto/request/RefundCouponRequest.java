package com.fsa.franchise.product_service.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class RefundCouponRequest {

    private UUID customerId;
    private UUID invoiceId;
    private BigDecimal amount;

}
