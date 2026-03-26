package com.fsa.franchise.product_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPerformanceRequest {
    private UUID franchiseId;
    private LocalDate startDate;
    private LocalDate endDate;
}
