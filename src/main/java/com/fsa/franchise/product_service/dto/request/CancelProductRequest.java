package com.fsa.franchise.product_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CancelProductRequest {
    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "Order Item ID is required")
    private UUID orderItemId;

    private String reason;
}
