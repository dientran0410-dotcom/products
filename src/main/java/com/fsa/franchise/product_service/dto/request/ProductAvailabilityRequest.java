package com.fsa.franchise.product_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductAvailabilityRequest {

    @NotNull(message = "isAvailable flag is required")
    private Boolean isAvailable;
}