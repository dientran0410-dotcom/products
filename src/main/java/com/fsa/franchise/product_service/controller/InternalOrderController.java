package com.fsa.franchise.product_service.controller;

import com.fsa.franchise.product_service.dto.request.ProductPerformanceRequest;
import com.fsa.franchise.product_service.dto.response.ExternalOrderItemResponse;
import com.fsa.franchise.product_service.dto.response.ExternalOrderResponse;
import com.fsa.franchise.product_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products/public/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {

    private final OrderService orderService;

    @GetMapping("/report")
    public ResponseEntity<List<ExternalOrderResponse>> getOrdersForReport(
            @RequestParam(required = false) UUID franchiseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(orderService.getOrdersForReport(franchiseId, startDate, endDate));
    }

    @GetMapping("/report/all")
    public ResponseEntity<List<ExternalOrderResponse>> getAllOrdersForReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(orderService.getAllOrdersForReport(startDate, endDate));
    }

    @GetMapping("/report/items")
    public ResponseEntity<List<ExternalOrderItemResponse>> getOrderItemsForReport(
            @RequestParam(required = false) UUID franchiseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(orderService.getOrderItemsForReport(franchiseId, startDate, endDate));
    }

    @GetMapping("/customer")
    public ResponseEntity<List<ExternalOrderResponse>> getOrdersForCustomer(
            @RequestParam(required = false) UUID customerId) {
        return ResponseEntity.ok(orderService.getOrdersForCustomer(customerId));
    }
}
