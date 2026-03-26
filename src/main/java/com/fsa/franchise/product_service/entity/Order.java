package com.fsa.franchise.product_service.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fsa.franchise.product_service.entity.base.BaseAuditEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "franchise_id", nullable = false)
    private UUID franchiseId;

    @Column(name = "customer_id")
    private UUID customerId;

    public enum OrderSource {
        APP, POS
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "order_source", nullable = false, length = 20)
    private OrderSource orderSource;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    public enum OrderStatus {
        PENDING, CONFIRMED, CANCELLED, PREPARING, COMPLETED
    }

    public enum PaymentStatus {
        UNPAID, PAID, REFUNDED
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "notes")
    private String notes;

    @Version
    @Builder.Default
    private Integer version = 0;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "estimated_preparation_time_minutes", nullable = false)
    @Builder.Default
    private int estimatedPreparationTimeMinutes = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean flagged = false;

    @Column(name = "flag_reason", length = 255)
    private String flagReason;

    @Column(name = "flag_notes")
    private String flagNotes;

    @Column(name = "flagged_at")
    private LocalDateTime flaggedAt;

    @Column(name = "assigned_staff_id")
    private UUID assignedStaffId;

}
