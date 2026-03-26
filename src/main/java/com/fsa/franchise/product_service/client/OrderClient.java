package com.fsa.franchise.product_service.client;

import com.fsa.franchise.product_service.dto.response.OrderResponseV2;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "order-service", url = "https://microservice-i7nc.onrender.com")

public interface OrderClient {

        @GetMapping("/orders/{id}")
        OrderResponseV2 getOrder(
                        @PathVariable Long id);

        @PutMapping("/api/orders/{orderId}/status")
        void updateOrderStatus(
                        @PathVariable UUID orderId,
                        @RequestParam String status);
}
