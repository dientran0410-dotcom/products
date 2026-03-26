package com.fsa.franchise.product_service.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EngagementCouponRequest {

    private Long promotionId;

    private String code;

    private String discountType;

    private Double discountValue;

    private Double minOrderValue;

    private Double maxDiscount;

    private Integer usageLimit;

    private Integer userLimit;

    private Long minTierId;

    private Boolean isPublic;
}
