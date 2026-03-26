package com.fsa.franchise.product_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReassignOrderRequest {

    private UUID userID;
    @NotNull(message = "newStaffId must not be null")
    private UUID newStaffId;
}
