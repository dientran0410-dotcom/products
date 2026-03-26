package com.fsa.franchise.product_service.controller;

import com.fsa.franchise.product_service.client.OrderClient;
import com.fsa.franchise.product_service.dto.request.MomoIpnRequest;
import com.fsa.franchise.product_service.dto.response.PaymentResponse;
import com.fsa.franchise.product_service.entity.PaymentTransaction;
import com.fsa.franchise.product_service.model.CreateMomoRequest;
import com.fsa.franchise.product_service.model.CreateMomoResponse;
import com.fsa.franchise.product_service.repository.PaymentTransactionRepository;
import com.fsa.franchise.product_service.service.MomoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/products/momo")
public class MomoController {

    private final MomoService momoService;

    @PostMapping("/create")
    public CreateMomoResponse create(@RequestBody CreateMomoRequest req)
            throws Exception {
        return momoService.create(req);

    }

    @PostMapping("/ipn")
    public ResponseEntity<?> ipn(@RequestBody MomoIpnRequest data) {

        try {
            momoService.handleIpn(data);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("IPN processing failed");
        }
    }

    @GetMapping("/return")
    public ResponseEntity<PaymentResponse> momoReturn(@RequestParam Map<String, String> params) {
        PaymentResponse response = momoService.handleReturn(params);
        return ResponseEntity.ok().body(response);
    }
}
