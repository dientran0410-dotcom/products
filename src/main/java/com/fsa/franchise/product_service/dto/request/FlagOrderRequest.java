package com.fsa.franchise.product_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FlagOrderRequest {

    @NotBlank(message = "Reason is required")
    private String reason;

    private String notes;
}
