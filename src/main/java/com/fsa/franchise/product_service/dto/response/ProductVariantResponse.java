package com.fsa.franchise.product_service.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;


import java.math.BigDecimal;
import java.util.UUID;


@Getter
@Setter
public class ProductVariantResponse {
    private UUID id;
    private String sku;
    private String name;
    @Schema
    private BigDecimal price;
    private boolean defaultVariant;
}








