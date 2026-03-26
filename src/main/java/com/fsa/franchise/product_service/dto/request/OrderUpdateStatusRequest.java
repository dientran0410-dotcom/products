package com.fsa.franchise.product_service.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderUpdateStatusRequest {

    private UUID userID;
    @NotNull(message = "New status is required")
    private String newStatus;
    private String reason;
}