package com.fsa.franchise.product_service.service;

import java.util.UUID;

public interface PaymentService {
        String createPayment(UUID invoiceId);

        String refundPayment(UUID invoiceId);
}
