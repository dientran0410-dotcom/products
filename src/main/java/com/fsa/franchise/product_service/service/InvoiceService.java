package com.fsa.franchise.product_service.service;

import com.fsa.franchise.product_service.dto.response.InvoiceResponse;
import com.fsa.franchise.product_service.entity.Invoice;

import java.util.UUID;

public interface InvoiceService {
    Invoice createInvoice(UUID customerId);

    Invoice applyCouponCode(UUID invoiceId, String couponCode, java.math.BigDecimal discountPercent);

    Invoice applyPoints(UUID invoiceId, int points);

    InvoiceResponse checkout(UUID invoiceId);

    Invoice getInvoice(UUID invoiceId);
}
