package com.fsa.franchise.product_service.controller;

import com.fsa.franchise.product_service.dto.request.*;
import com.fsa.franchise.product_service.dto.response.*;
import com.fsa.franchise.product_service.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import com.fsa.franchise.product_service.service.LiveOrderService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final LiveOrderService liveOrderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderCreateRequest request) {

        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/history")
    public ResponseEntity<OrderHistoryResponse> getOrderHistorySummary(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @PathVariable UUID customerId) {

        OrderHistoryResponse response = orderService.getOrderHistorySummary(customerId, page, pageSize, status,
                fromDate, toDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(
            @PathVariable UUID id,
            @PathVariable UUID customerId) {
        OrderDetailResponse response = orderService.getOrderDetail(id, customerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    public ResponseEntity<OrderListResponse> getOrderList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @PathVariable UUID franchiseId) {

        UUID currentManagerFranchiseId = franchiseId;
        OrderListResponse response = orderService.getOrderListForManager(
                currentManagerFranchiseId, page, pageSize, status, fromDate, toDate);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/items/{itemId}/cancel")
    public ResponseEntity<OrderResponse> cancelProduct(
            @PathVariable UUID orderId,
            @PathVariable UUID itemId,
            @RequestParam(required = false) String reason,
            @PathVariable UUID userId) {

        String currentUser = userId.toString();
        CancelProductRequest request = new CancelProductRequest();
        request.setOrderId(orderId);
        request.setOrderItemId(itemId);
        request.setReason(reason);

        OrderResponse response = orderService.cancelProduct(request, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<OrderStatusResponse> getOrderStatus(@PathVariable UUID orderId) {
        OrderStatusResponse response = orderService.getOrderStatus(orderId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable UUID id,
            @PathVariable UUID customerId) {

        OrderResponse response = orderService.cancelOrderByCustomer(id, customerId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/staff-cancel")
    public ResponseEntity<OrderResponse> managerCancelOrder(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason,
            @PathVariable UUID userID,
            @PathVariable String role) {

        String managerId = userID.toString();
        if (role == null)
            role = "MANAGER";

        CancelOrderRequest request = new CancelOrderRequest();
        request.setReason(reason);

        OrderResponse response = orderService.cancelOrderByStaff(id, request, managerId, role);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/payment-callback")
    public ResponseEntity<OrderResponse> paymentCallback(
            @PathVariable UUID id,
            @RequestBody PaymentCallbackRequest request) {

        OrderResponse response = orderService.processPaymentCallback(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderUpdateStatusRequest request) {

        String staffId = request.getUserID().toString();
        OrderResponse response = orderService.updateOrderStatus(orderId, request, staffId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderUpdateRequest request) {

        String staffId = request.getUserID().toString();
        OrderResponse response = orderService.updateOrder(orderId, request, staffId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/estimate-time")
    public ResponseEntity<EstimatePreparationTimeResponse> estimateOrderTime(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.estimatePreparationTime(id));
    }

    @PostMapping("/{orderId}/flag")
    public ResponseEntity<FlagOrderResponse> flagOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody FlagOrderRequest request) {
        return ResponseEntity.ok(orderService.flagOrder(orderId, request));
    }

    @Operation(summary = "Stream live orders", description = "Connect to an SSE stream to receive real-time order updates")
    @GetMapping(value = "/live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamLiveOrders(@AuthenticationPrincipal Jwt jwt) {

        String role = jwt.getClaimAsString("role");

        if (!"STAFF".equalsIgnoreCase(role) && !"MANAGER".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }

        return liveOrderService.connect();
    }

    @Operation(summary = "Reassign order to another staff member", description = "Manager reassigns an active order to a different staff member. Order must not be COMPLETED or CANCELLED.")
    @PutMapping("/{orderId}/reassign")
    public ResponseEntity<Map<String, String>> reassignOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody ReassignOrderRequest request) {

        String managerId = request.getUserID().toString();
        Map<String, String> response = orderService.reassignOrder(orderId, request, managerId);
        return ResponseEntity.ok(response);
    }

}