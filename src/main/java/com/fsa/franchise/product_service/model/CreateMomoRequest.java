package com.fsa.franchise.product_service.model;

import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class CreateMomoRequest {
    private UUID orderId;
    private Long amount;
}
