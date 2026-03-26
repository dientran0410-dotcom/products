package com.fsa.franchise.product_service.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fsa.franchise.product_service.entity.Order.OrderStatus;

@Getter
@Builder
public class OrderHistorySummary {
    private UUID orderId;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal totalAmount;
}