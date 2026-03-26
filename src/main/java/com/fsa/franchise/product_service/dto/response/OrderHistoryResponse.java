package com.fsa.franchise.product_service.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class OrderHistoryResponse {
    private long total;
    private int page;
    private int pageSize;
    private List<OrderHistorySummary> orders;
}