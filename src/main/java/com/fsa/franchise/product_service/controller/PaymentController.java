package com.fsa.franchise.product_service.controller;

import com.fsa.franchise.product_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("api/products/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/{invoiceId}")
    public String createPayment(@PathVariable UUID invoiceId) {

        return paymentService.createPayment(invoiceId);
    }

    @PostMapping("/refund/{invoiceId}")
    public String refundPayment(@PathVariable UUID invoiceId) {
        return paymentService.refundPayment(invoiceId);
    }
}
