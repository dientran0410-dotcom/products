package com.fsa.franchise.product_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_audit_logs")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    public enum EntityType {
        ORDER, ORDER_ITEM, ORDER_ITEM_ADDON
    }

    public enum ActionType {
        CREATE, UPDATE, CANCEL, CANCEL_ENTIRE_ORDER, STATUS_CHANGE, REASSIGN
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 30)
    private EntityType entityType;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20)
    private ActionType actionType;

    @Column(name = "changed_fields", nullable = false, length = 255)
    private String changedFields;

    @Column(name = "reason")
    private String reason;

    @CreatedBy
    @Column(name = "changed_by", nullable = false, length = 50, updatable = false)
    private String changedBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}