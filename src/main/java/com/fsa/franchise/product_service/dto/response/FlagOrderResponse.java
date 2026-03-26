package com.fsa.franchise.product_service.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FlagOrderResponse {

    private UUID orderId;
    private Boolean flagged;
    private String reason;
    private String notes;
    private LocalDateTime flaggedAt;
}
