package com.fsa.franchise.product_service.entity;

import lombok.*;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cart_item")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter

public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long cartId;

    @Column(nullable = false)
    private UUID variantId;

    private UUID productId;

    private Integer quantity;

    private BigDecimal price;
}
