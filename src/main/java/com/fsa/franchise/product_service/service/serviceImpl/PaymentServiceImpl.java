package com.fsa.franchise.product_service.service.serviceImpl;

import com.fsa.franchise.product_service.mapper.CouponMapper;
import com.fsa.franchise.product_service.client.CouponClient;
import com.fsa.franchise.product_service.dto.request.EngagementCouponRequest;
import com.fsa.franchise.product_service.dto.request.RefundCouponRequest;
import com.fsa.franchise.product_service.entity.Invoice;
import com.fsa.franchise.product_service.model.CreateMomoRequest;
import com.fsa.franchise.product_service.model.CreateMomoResponse;
import com.fsa.franchise.product_service.repository.InvoiceRepository;
import com.fsa.franchise.product_service.service.MomoService;
import com.fsa.franchise.product_service.service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final InvoiceRepository invoiceRepository;
    private final MomoService momoService;
    private final CouponClient couponClient;

    @Override
    public String createPayment(UUID invoiceId) {

        try {

            Invoice invoice = invoiceRepository
                    .findById(invoiceId)
                    .orElseThrow(() -> new RuntimeException("Invoice not found"));

            CreateMomoRequest request = new CreateMomoRequest();
            request.setOrderId(invoice.getId());
            request.setAmount(invoice.getTotalAmount().longValue());

            CreateMomoResponse response = momoService.create(request);

            return response.getPayUrl();

        } catch (Exception e) {
            throw new RuntimeException("Create momo payment failed", e);
        }
    }

    @Override
    @Transactional
    public String refundPayment(UUID invoiceId) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (invoice.getStatus() == Invoice.InvoiceStatus.REFUNDED) {
            throw new RuntimeException("Invoice already refunded");
        }

        if (invoice.getStatus() != Invoice.InvoiceStatus.PAID) {
            throw new RuntimeException("Invoice is not paid");
        }

        if (invoice.getTotalAmount() == null || invoice.getTotalAmount().doubleValue() <= 0) {
            throw new RuntimeException("Total amount not set");
        }

        if (invoice.getCustomerId() == null) {
            throw new RuntimeException("Customer is required");
        }

        RefundCouponRequest refundCouponRequest = new RefundCouponRequest();
        refundCouponRequest.setInvoiceId(invoice.getId());
        refundCouponRequest.setCustomerId(invoice.getCustomerId());
        refundCouponRequest.setAmount(invoice.getTotalAmount());

        EngagementCouponRequest couponRequest = CouponMapper.toEngagementCoupon(refundCouponRequest);

        try {
            couponClient.createRefundCoupon(couponRequest);
        } catch (Exception e) {
            throw new RuntimeException("Refund coupon failed - cannot create coupon", e);
        }

        invoice.setStatus(Invoice.InvoiceStatus.REFUNDED);
        invoiceRepository.save(invoice);

        return "Refund success - Coupon created";
    }
}
