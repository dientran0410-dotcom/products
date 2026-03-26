package com.fsa.franchise.product_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ingredients")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "base_unit", nullable = false, length = 20)
    private Unit unit;

    public enum Unit {
        GRAM(0.001),
        KILOGRAM(1.0),
        MILLILITER(0.001),
        LITER(1.0),
        PIECE(1.0);

        private final double toBase;

        Unit(double toBase) {
            this.toBase = toBase;
        }
    }

    public enum IngredientStatus {
        ACTIVE, INACTIVE
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private IngredientStatus status = IngredientStatus.ACTIVE;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "preparation_time_minutes", nullable = false)
    @Builder.Default
    private int preparationTimeMinutes = 0;

}
