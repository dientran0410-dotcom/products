package com.fsa.franchise.product_service.controller;

import com.fsa.franchise.product_service.dto.response.InvoiceResponse;
import com.fsa.franchise.product_service.entity.Invoice;
import com.fsa.franchise.product_service.repository.InvoiceRepository;
import com.fsa.franchise.product_service.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;

    @PostMapping("/create/{customerId}")
    public Invoice createInvoice(@PathVariable UUID customerId) {
        return invoiceService.createInvoice(customerId);
    }

    @PostMapping("/{invoiceId}/coupon")
    public Invoice applyCoupon(
            @PathVariable UUID invoiceId,
            @RequestParam String couponCode,
            @RequestParam BigDecimal discountPercent) {

        return invoiceService.applyCouponCode(invoiceId, couponCode, discountPercent);
    }

    @PostMapping("/{invoiceId}/points")
    public Invoice applyPoints(
            @PathVariable UUID invoiceId,
            @RequestParam int points) {

        return invoiceService.applyPoints(invoiceId, points);
    }

    @PostMapping("/{invoiceId}/checkout")
    public InvoiceResponse checkout(@PathVariable UUID invoiceId) {
        return invoiceService.checkout(invoiceId);
    }

    @GetMapping("/{invoiceId}")
    public Invoice getInvoice(@PathVariable UUID invoiceId) {

        return invoiceService.getInvoice(invoiceId);
    }

    @GetMapping
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }
}
