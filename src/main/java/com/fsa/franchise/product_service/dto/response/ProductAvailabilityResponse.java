package com.fsa.franchise.product_service.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class ProductAvailabilityResponse {
    private UUID id;
    private Boolean isAvailable;
}
