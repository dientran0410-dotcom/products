package com.fsa.franchise.product_service.entity;

import com.stripe.model.PaymentMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refund")
@Getter
@Setter
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long paymentId;

    private BigDecimal amount;

    private String reason;

    private String providerRefundId;

    @Enumerated(EnumType.STRING)
    private RefundStatus status;

    private Long createdBy;

    private LocalDateTime createdAt;

    public enum RefundStatus {
        PENDING,
        SUCCESS,
        FAILED
    }

}
