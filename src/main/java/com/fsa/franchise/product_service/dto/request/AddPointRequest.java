package com.fsa.franchise.product_service.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class AddPointRequest {

    private UUID customerId;
    private Long points;
}
