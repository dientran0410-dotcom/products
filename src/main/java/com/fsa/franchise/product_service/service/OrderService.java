package com.fsa.franchise.product_service.service;

import com.fsa.franchise.product_service.dto.request.*;
import com.fsa.franchise.product_service.dto.request.CancelOrderRequest;
import com.fsa.franchise.product_service.dto.request.CancelProductRequest;
import com.fsa.franchise.product_service.dto.request.OrderCreateRequest;
import com.fsa.franchise.product_service.dto.request.OrderUpdateRequest;
import com.fsa.franchise.product_service.dto.request.OrderUpdateStatusRequest;
import com.fsa.franchise.product_service.dto.request.PaymentCallbackRequest;
import com.fsa.franchise.product_service.dto.request.PosOrderRequest;
import com.fsa.franchise.product_service.dto.response.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface OrderService {
        OrderResponse createOrder(OrderCreateRequest request);

        OrderHistoryResponse getOrderHistorySummary(UUID customerId, int page, int pageSize, String status,
                        LocalDate fromDate, LocalDate toDate);

        OrderDetailResponse getOrderDetail(UUID orderId, UUID customerId);

        OrderListResponse getOrderListForManager(UUID franchiseId, int page, int pageSize, String status,
                        LocalDate fromDate, LocalDate toDate);

        OrderResponse cancelProduct(CancelProductRequest request, String changedBy);

        OrderStatusResponse getOrderStatus(UUID orderId);

        OrderResponse cancelOrderByCustomer(UUID orderId, UUID customerId);

        OrderResponse cancelOrderByStaff(UUID orderId, CancelOrderRequest request, String staffId, String role);

        OrderResponse processPaymentCallback(UUID orderId, PaymentCallbackRequest request);

        OrderResponse createPosOrder(PosOrderRequest request, UUID franchiseId, String staffId);

        OrderResponse updateOrderStatus(UUID orderId, OrderUpdateStatusRequest request, String staffId);

        OrderResponse updateOrder(UUID orderId, OrderUpdateRequest request, String staffId);

        EstimatePreparationTimeResponse estimatePreparationTime(UUID orderId);

        FlagOrderResponse flagOrder(UUID orderId, FlagOrderRequest request);

        Map<String, String> reassignOrder(UUID orderId, ReassignOrderRequest request, String managerId);

        List<ExternalOrderResponse> getOrdersForReport(UUID franchiseId, LocalDate startDate, LocalDate endDate);

        List<ExternalOrderResponse> getAllOrdersForReport(LocalDate startDate, LocalDate endDate);

        List<ExternalOrderItemResponse> getOrderItemsForReport(UUID franchiseId, LocalDate startDate,
                        LocalDate endDate);

        List<ExternalOrderResponse> getOrdersForCustomer(UUID customerId);

}