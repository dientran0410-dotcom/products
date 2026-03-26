package com.fsa.franchise.product_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID orderId;

    private String requestId;

    private String momoTransId;

    private Long amount;

    private String status;

    private String paymentMethod;

    private Integer resultCode;

    private String message;

    private String payUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String momoOrderId;

}
