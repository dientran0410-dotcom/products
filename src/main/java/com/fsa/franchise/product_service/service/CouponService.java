package com.fsa.franchise.product_service.service;

import java.math.BigDecimal;

public interface CouponService {
    void createCoupon(Long customerId, BigDecimal amount);
}
