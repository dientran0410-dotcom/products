package com.fsa.franchise.product_service.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class OrderListResponse {
    private long total;
    private int page;
    private int pageSize;
    private List<OrderListSummary> orders;
}