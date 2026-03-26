package com.fsa.franchise.product_service.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fsa.franchise.product_service.entity.Order.OrderStatus;
import com.fsa.franchise.product_service.entity.Order.PaymentStatus;

@Getter
@Builder
public class OrderDetailResponse {
    private UUID orderId;
    private String orderNumber;
    private String orderSource;
    private UUID customerId;
    private String customerName;
    private String customerPhone;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    private String notes;
    private List<OrderItemDetail> items;
}