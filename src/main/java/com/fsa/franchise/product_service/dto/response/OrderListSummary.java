package com.fsa.franchise.product_service.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fsa.franchise.product_service.entity.Order.OrderSource;
import com.fsa.franchise.product_service.entity.Order.OrderStatus;
import com.fsa.franchise.product_service.entity.Order.PaymentStatus;

@Getter
@Builder
public class OrderListSummary {
    private UUID orderId;
    private String orderNumber;
    private UUID customerId;
    private PaymentStatus paymentStatus;
    private OrderSource orderSource;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime orderDate;
}
