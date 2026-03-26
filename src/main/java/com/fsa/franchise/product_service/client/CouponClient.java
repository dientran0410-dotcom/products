package com.fsa.franchise.product_service.client;

import com.fsa.franchise.product_service.dto.request.EngagementCouponRequest;
import com.fsa.franchise.product_service.dto.request.RefundCouponRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "coupon-service", url = "https://microservice-i7nc.onrender.com/api/v1")
public interface CouponClient {

    @PostMapping("/api/engagement/coupons")
    void createRefundCoupon(@RequestBody EngagementCouponRequest request);

}
