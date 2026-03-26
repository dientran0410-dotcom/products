package com.fsa.franchise.product_service.dto.response;

import jakarta.persistence.Entity;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class CartResponse {
    private Long cartId;

    private List<CartItemResponse> items;

    private BigDecimal subtotal;

}
