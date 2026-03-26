package com.fsa.franchise.product_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveOrderDto {
    private UUID orderId;
    private String customerName; // if available, else null
    private List<LiveOrderItemDto> items;
    private String currentStatus; // ORDERED, PREPARING, COMPLETED
    private LocalDateTime createdTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LiveOrderItemDto {
        private String productName;
        private Integer quantity;
        private String notes;
        private List<String> addons;
    }
}
