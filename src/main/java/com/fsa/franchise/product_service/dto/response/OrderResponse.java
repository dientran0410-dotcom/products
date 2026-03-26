package com.fsa.franchise.product_service.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fsa.franchise.product_service.entity.Order.PaymentStatus;

@Getter
@Setter
@Builder
public class OrderResponse {
    private UUID id;
    private String orderNumber;
    private String status;
    private BigDecimal totalAmount;
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;
}