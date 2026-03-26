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
public class ExternalOrderResponse {
    private UUID id;
    private UUID franchiseId;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
}
