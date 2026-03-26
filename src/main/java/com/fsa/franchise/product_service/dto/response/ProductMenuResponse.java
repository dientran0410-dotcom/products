package com.fsa.franchise.product_service.dto.response;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProductMenuResponse {
    private UUID productId;
    private String productName;
}
