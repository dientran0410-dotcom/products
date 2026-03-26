package com.fsa.franchise.product_service.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue
    @org.hibernate.annotations.UuidGenerator
    private UUID id;

    private String code;

    private UUID orderId;

    private UUID customerId;

    @Column(name = "sub_total")
    private BigDecimal subtotal;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "points_discount")
    private BigDecimal pointsDiscount;

    @Column(name = "tax_amount")
    private BigDecimal taxAmount;

    @Column(name = "shipping_fee")
    private BigDecimal shippingFee;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    private String currency;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    @Column(name = "franchise_id")
    private Long franchiseId;

    private LocalDateTime issuedAt;

    private LocalDateTime paidAt;

    private LocalDateTime cancelledAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<InvoiceItem> items = new ArrayList<>();

    public enum InvoiceStatus {
        DRAFT,
        PENDING_PAYMENT,
        PAID,
        FAILED,
        CANCELLED,
        REFUNDED
    }
}
