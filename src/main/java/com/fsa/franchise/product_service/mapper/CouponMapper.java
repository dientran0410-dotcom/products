package com.fsa.franchise.product_service.mapper;

import com.fsa.franchise.product_service.dto.request.RefundCouponRequest;
import com.fsa.franchise.product_service.dto.request.EngagementCouponRequest;

public class CouponMapper {
    public static EngagementCouponRequest toEngagementCoupon(RefundCouponRequest request) {

        double amount = request.getAmount().doubleValue();

        return EngagementCouponRequest.builder()
                .promotionId(null)
                .code("REFUND_" + request.getInvoiceId() + "_" + System.currentTimeMillis())
                .discountType("FIXED")
                .discountValue(amount)
                .minOrderValue(0.0)
                .maxDiscount(amount)
                .usageLimit(1)
                .userLimit(1)
                .minTierId(null)
                .isPublic(false) // chỉ user này dùng
                .build();
    }

    private static String generateCode(Long invoiceId) {
        return "REFUND_" + invoiceId + "_" + System.currentTimeMillis();
    }
}
