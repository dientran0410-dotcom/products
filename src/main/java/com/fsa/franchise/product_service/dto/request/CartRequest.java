package com.fsa.franchise.product_service.dto.request;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CartRequest {

    private UUID customerId;
    private UUID productId;
    private UUID variantId;
    private Integer quantity;

}
