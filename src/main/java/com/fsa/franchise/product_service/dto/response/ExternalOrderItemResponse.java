package com.fsa.franchise.product_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalOrderItemResponse {
    private UUID productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String orderStatus;
    private LocalDateTime createdAt;
}
