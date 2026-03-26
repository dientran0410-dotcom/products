package com.fsa.franchise.product_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalProductResponse {
    private UUID id;
    private String name;
    private BigDecimal price;
    private String category;
}
