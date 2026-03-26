package com.fsa.franchise.product_service.service.serviceImpl;

import com.fsa.franchise.product_service.dto.request.*;
import com.fsa.franchise.product_service.dto.response.*;
import com.fsa.franchise.product_service.entity.*;
import com.fsa.franchise.product_service.repository.OrderAuditLogRepository;
import com.fsa.franchise.product_service.repository.OrderItemRepository;
import com.fsa.franchise.product_service.repository.OrderRepository;
import com.fsa.franchise.product_service.repository.OrderStatusHistoryRepository;
import com.fsa.franchise.product_service.repository.VariantIngredientRepository;
import com.fsa.franchise.product_service.service.InventoryClientService;
import com.fsa.franchise.product_service.service.OrderService;
import com.fsa.franchise.product_service.service.ProductService;
import com.fsa.franchise.product_service.specification.OrderSpecification;
import com.fsa.franchise.product_service.service.ProductVariantService;
import com.fsa.franchise.product_service.event.OrderChangedEvent;
import com.fsa.franchise.product_service.event.OrderCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
        private final OrderRepository orderRepository;
        private final ApplicationEventPublisher eventPublisher;
        private final OrderAuditLogRepository orderAuditLogRepository;
        private final OrderStatusHistoryRepository orderStatusHistoryRepository;
        private final ProductVariantService productVariantService;
        private final ProductService productService;
        private final InventoryClientService inventoryClientService;
        private final VariantIngredientRepository variantIngredientRepository;
        private final OrderItemRepository orderItemRepository;

        @Override
        @Transactional
        public OrderResponse createOrder(OrderCreateRequest request) {
                Set<UUID> allIds = new HashSet<>();
                request.getItems().forEach(item -> {
                        allIds.add(item.getVariantId());
                        if (item.getAddons() != null) {
                                item.getAddons().forEach(addon -> allIds.add(addon.getAddonVariantId()));
                        }
                });

                Map<UUID, ProductVariantDto> productMap = productVariantService
                                .getVariantsByIds(new ArrayList<>(allIds))
                                .stream().collect(Collectors.toMap(ProductVariantDto::getId, p -> p));

                Order order = Order.builder()
                                .franchiseId(request.getFranchiseId())
                                .customerId(request.getCustomerId())
                                .orderSource(Order.OrderSource.valueOf(request.getOrderSource().toUpperCase()))
                                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                                .status(Order.OrderStatus.PENDING)
                                .paymentStatus(Order.PaymentStatus.UNPAID)
                                .notes(request.getNotes())
                                .items(new ArrayList<>())
                                .build();

                BigDecimal totalOrderAmount = BigDecimal.ZERO;

                for (OrderItemRequest itemReq : request.getItems()) {
                        ProductVariantDto variantInfo = productMap.get(itemReq.getVariantId());
                        validateProduct(variantInfo);

                        BigDecimal itemUnitPrice = variantInfo.getPrice();
                        BigDecimal itemQty = BigDecimal.valueOf(itemReq.getQuantity());
                        BigDecimal itemSubtotal = itemUnitPrice.multiply(itemQty);

                        OrderItem orderItem = OrderItem.builder()
                                        .order(order)
                                        .variantId(itemReq.getVariantId())
                                        .sku(variantInfo.getSku())
                                        .variantName(variantInfo.getName())
                                        .quantity(itemReq.getQuantity())
                                        .unitPrice(itemUnitPrice)
                                        .subtotal(itemSubtotal)
                                        .addons(new ArrayList<>())
                                        .build();

                        if (itemReq.getAddons() != null) {
                                for (OrderItemAddonRequest addonReq : itemReq.getAddons()) {
                                        ProductVariantDto addonInfo = productMap.get(addonReq.getAddonVariantId());
                                        BigDecimal addonSubtotal = addonInfo.getPrice()
                                                        .multiply(BigDecimal.valueOf(addonReq.getQuantity()))
                                                        .multiply(itemQty);

                                        orderItem.getAddons().add(OrderItemAddon.builder()
                                                        .orderItem(orderItem)
                                                        .addonVariantId(addonReq.getAddonVariantId())
                                                        .sku(addonInfo.getSku())
                                                        .addonName(addonInfo.getName())
                                                        .quantity(addonReq.getQuantity())
                                                        .unitPrice(addonInfo.getPrice())
                                                        .subtotal(addonSubtotal)
                                                        .build());

                                        itemSubtotal = itemSubtotal.add(addonSubtotal);
                                }
                        }
                        orderItem.setSubtotal(itemSubtotal);
                        order.getItems().add(orderItem);
                        totalOrderAmount = totalOrderAmount.add(itemSubtotal);
                }

                order.setTotalAmount(totalOrderAmount);
                Order savedOrder = orderRepository.save(order);

                // Enqueue notification — Kafka fires ONLY after TX commits (via
                // @TransactionalEventListener)
                eventPublisher.publishEvent(new OrderCreatedEvent(
                                this,
                                savedOrder.getId().toString(),
                                savedOrder.getOrderNumber(),
                                savedOrder.getTotalAmount(),
                                savedOrder.getCustomerId().toString()));

                eventPublisher.publishEvent(new OrderChangedEvent(this, savedOrder.getId()));

                return mapToResponse(savedOrder);
        }

        private void validateProduct(ProductVariantDto info) {
                if (info == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found");
                }
                if (info.getStatus() != ProductVariant.VariantStatus.ACTIVE) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is inactive");
                }
        }

        private OrderResponse mapToResponse(Order order) {
                return OrderResponse.builder()
                                .id(order.getId())
                                .orderNumber(order.getOrderNumber())
                                .status(order.getStatus().name())
                                .totalAmount(order.getTotalAmount())
                                .paymentStatus(order.getPaymentStatus())
                                .createdAt(order.getCreatedAt())
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public OrderHistoryResponse getOrderHistorySummary(UUID customerId, int page, int pageSize, String status,
                        LocalDate fromDate, LocalDate toDate) {
                if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromDate cannot be after toDate");
                }
                LocalDateTime startDateTime = (fromDate != null) ? fromDate.atStartOfDay() : null;
                LocalDateTime endDateTime = (toDate != null) ? toDate.atTime(LocalTime.MAX) : null;

                Specification<Order> spec = OrderSpecification.filterHistory(customerId, status, startDateTime,
                                endDateTime);
                int pageIndex = (page > 0) ? page - 1 : 0;
                Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

                Page<Order> orderPage = orderRepository.findAll(spec, pageable);

                List<OrderHistorySummary> orderDtos = orderPage.getContent().stream()
                                .map(order -> OrderHistorySummary.builder()
                                                .orderId(order.getId())
                                                .orderDate(order.getCreatedAt())
                                                .status(order.getStatus())
                                                .totalAmount(order.getTotalAmount())
                                                .build())
                                .toList();

                return OrderHistoryResponse.builder()
                                .total(orderPage.getTotalElements())
                                .page(page)
                                .pageSize(pageSize)
                                .orders(orderDtos)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public OrderDetailResponse getOrderDetail(UUID orderId, UUID customerId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Order not found"));

                if (!order.getCustomerId().equals(customerId)) {
                        throw new RuntimeException("Forbidden: You do not have permission to view this order");
                }

                List<OrderItemDetail> itemDtos = order.getItems().stream().map(item -> {

                        List<OrderItemAddonDetail> addonDtos = item.getAddons().stream()
                                        .map(addon -> OrderItemAddonDetail.builder()
                                                        .addonName(addon.getAddonName())
                                                        .quantity(addon.getQuantity())
                                                        .unitPrice(addon.getUnitPrice())
                                                        .subtotal(addon.getSubtotal())
                                                        .build())
                                        .toList();

                        return OrderItemDetail.builder()
                                        .variantId(item.getVariantId())
                                        .sku(item.getSku())
                                        .productName(item.getVariantName())
                                        .quantity(item.getQuantity())
                                        .unitPrice(item.getUnitPrice())
                                        .subtotal(item.getSubtotal())
                                        .notes(item.getNotes())
                                        .addons(addonDtos)
                                        .build();
                }).toList();

                return OrderDetailResponse.builder()
                                .orderId(order.getId())
                                .orderNumber(order.getOrderNumber())
                                .orderSource(order.getOrderSource().name())
                                .status(order.getStatus())
                                .customerId(order.getCustomerId())
                                .customerName("Nguyen Van A")
                                .customerPhone("0953248761")
                                .paymentStatus(order.getPaymentStatus())
                                .totalAmount(order.getTotalAmount())
                                .orderDate(order.getCreatedAt())
                                .notes(order.getNotes())
                                .items(itemDtos)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public OrderListResponse getOrderListForManager(UUID franchiseId, int page, int pageSize, String status,
                        LocalDate fromDate, LocalDate toDate) {
                LocalDateTime startDateTime = (fromDate != null) ? fromDate.atStartOfDay() : null;
                LocalDateTime endDateTime = (toDate != null) ? toDate.atTime(LocalTime.MAX) : null;

                Specification<Order> spec = OrderSpecification.filterOrderListForManager(franchiseId, status,
                                startDateTime,
                                endDateTime);

                int pageIndex = (page > 0) ? page - 1 : 0;
                Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

                Page<Order> orderPage = orderRepository.findAll(spec, pageable);

                List<OrderListSummary> orderDtos = orderPage.getContent().stream()
                                .map(order -> OrderListSummary.builder()
                                                .orderId(order.getId())
                                                .orderNumber(order.getOrderNumber())
                                                .orderSource(order.getOrderSource())
                                                .customerId(order.getCustomerId())
                                                .paymentStatus(order.getPaymentStatus())
                                                .totalAmount(order.getTotalAmount())
                                                .status(order.getStatus())
                                                .orderDate(order.getCreatedAt())
                                                .build())
                                .toList();

                return OrderListResponse.builder()
                                .total(orderPage.getTotalElements())
                                .page(page)
                                .pageSize(pageSize)
                                .orders(orderDtos)
                                .build();
        }

        @Override
        @Transactional
        public OrderResponse cancelProduct(CancelProductRequest request, String changedBy) {
                Order order = orderRepository.findById(request.getOrderId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Order not found"));

                OrderItem item = order.getItems().stream()
                                .filter(i -> i.getId().equals(request.getOrderItemId()))
                                .findFirst()
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Order item not found"));

                order.getItems().remove(item);

                BigDecimal newTotal = order.getItems().stream()
                                .map(OrderItem::getSubtotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                order.setTotalAmount(newTotal);

                order.setUpdatedAt(LocalDateTime.now());
                order.setUpdatedBy(changedBy);
                order.setStatus(Order.OrderStatus.CANCELLED);
                Order savedOrder = orderRepository.save(order);

                OrderAuditLog log = OrderAuditLog.builder()
                                .order(order)
                                .entityType(OrderAuditLog.EntityType.ORDER_ITEM)
                                .actionType(OrderAuditLog.ActionType.CANCEL)
                                .changedFields("items")
                                .reason(request.getReason())
                                .changedBy(changedBy)
                                .build();
                orderAuditLogRepository.save(log);

                return mapToResponse(savedOrder);
        }

        @Override
        @Transactional(readOnly = true)
        public OrderStatusResponse getOrderStatus(UUID orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Order not found"));

                List<OrderStatusHistory> history = orderStatusHistoryRepository.findByOrderId(orderId)
                                .stream()
                                .sorted(Comparator.comparing(OrderStatusHistory::getCreatedAt))
                                .toList();

                List<StatusHistoryDto> historyDtos = history.stream()
                                .map(h -> StatusHistoryDto.builder()
                                                .status(h.getNewStatus().name())
                                                .reason(h.getReason())
                                                .changedBy(h.getChangedBy())
                                                .changedAt(h.getCreatedAt())
                                                .build())
                                .toList();

                return OrderStatusResponse.builder()
                                .orderId(order.getId())
                                .currentStatus(order.getStatus().name())
                                .history(historyDtos)
                                .build();
        }

        @Override
        @Transactional
        public OrderResponse processPaymentCallback(UUID orderId, PaymentCallbackRequest request) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Order not found"));

                if ("SUCCESS".equalsIgnoreCase(request.getStatus())) {
                        order.setPaymentStatus(Order.PaymentStatus.PAID);
                        order.setStatus(Order.OrderStatus.CONFIRMED);

                        order.setUpdatedAt(LocalDateTime.now());
                        order.setUpdatedBy("SYSTEM_WEBHOOK");
                        Order savedOrder = orderRepository.save(order);

                        orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                                        .order(savedOrder)
                                        .newStatus(Order.OrderStatus.CONFIRMED)
                                        .reason("Payment successful: " + request.getTransactionId())
                                        .changedBy("SYSTEM_WEBHOOK")
                                        .build());

                        System.out.println(
                                        "[MOCK] Đã nhận thanh toán. Thông báo tới NotificationService: Gửi SMS cho khách hàng.");

                        eventPublisher.publishEvent(new OrderChangedEvent(this, savedOrder.getId()));
                        return mapToResponse(savedOrder);

                } else {
                        order.setPaymentStatus(Order.PaymentStatus.UNPAID);
                        System.out.println("[MOCK] Thanh toán thất bại. Bỏ qua ghi nhận.");
                        return mapToResponse(orderRepository.save(order));
                }
        }

        @Override
        @Transactional
        public OrderResponse createPosOrder(PosOrderRequest request, UUID franchiseId, String staffId) {
                Set<UUID> allIds = new HashSet<>();
                request.getItems().forEach(item -> {
                        allIds.add(item.getVariantId());
                        if (item.getAddons() != null) {
                                item.getAddons().forEach(addon -> allIds.add(addon.getAddonVariantId()));
                        }
                });

                Map<UUID, ProductVariantDto> productMap = productVariantService
                                .getVariantsByIds(new ArrayList<>(allIds))
                                .stream().collect(Collectors.toMap(ProductVariantDto::getId, p -> p));

                Order order = Order.builder()
                                .franchiseId(franchiseId)
                                .customerId(request.getCustomerId())
                                .orderSource(Order.OrderSource.POS)
                                .orderNumber("POS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                                .status(Order.OrderStatus.PENDING)
                                .paymentStatus(Order.PaymentStatus.UNPAID)
                                .notes("Payment Method: " + request.getPaymentMethod())
                                .items(new ArrayList<>())
                                .build();

                BigDecimal totalOrderAmount = BigDecimal.ZERO;

                for (OrderItemRequest itemReq : request.getItems()) {
                        ProductVariantDto variantInfo = productMap.get(itemReq.getVariantId());
                        validateProduct(variantInfo);

                        BigDecimal itemUnitPrice = variantInfo.getPrice();
                        BigDecimal itemQty = BigDecimal.valueOf(itemReq.getQuantity());
                        BigDecimal itemSubtotal = itemUnitPrice.multiply(itemQty);

                        OrderItem orderItem = OrderItem.builder()
                                        .order(order)
                                        .variantId(itemReq.getVariantId())
                                        .sku(variantInfo.getSku())
                                        .variantName(variantInfo.getName())
                                        .quantity(itemReq.getQuantity())
                                        .unitPrice(itemUnitPrice)
                                        .subtotal(itemSubtotal)
                                        .addons(new ArrayList<>())
                                        .build();

                        if (itemReq.getAddons() != null) {
                                for (OrderItemAddonRequest addonReq : itemReq.getAddons()) {
                                        ProductVariantDto addonInfo = productMap.get(addonReq.getAddonVariantId());
                                        BigDecimal addonSubtotal = addonInfo.getPrice()
                                                        .multiply(BigDecimal.valueOf(addonReq.getQuantity()))
                                                        .multiply(itemQty);

                                        orderItem.getAddons().add(OrderItemAddon.builder()
                                                        .orderItem(orderItem)
                                                        .addonVariantId(addonReq.getAddonVariantId())
                                                        .sku(addonInfo.getSku())
                                                        .addonName(addonInfo.getName())
                                                        .quantity(addonReq.getQuantity())
                                                        .unitPrice(addonInfo.getPrice())
                                                        .subtotal(addonSubtotal)
                                                        .build());
                                        itemSubtotal = itemSubtotal.add(addonSubtotal);
                                }
                        }
                        orderItem.setSubtotal(itemSubtotal);
                        order.getItems().add(orderItem);
                        totalOrderAmount = totalOrderAmount.add(itemSubtotal);
                }

                order.setTotalAmount(totalOrderAmount);

                order.setCreatedAt(LocalDateTime.now());
                order.setCreatedBy(staffId);
                Order savedOrder = orderRepository.save(order);

                eventPublisher.publishEvent(new OrderChangedEvent(this, savedOrder.getId()));
                return mapToResponse(savedOrder);

        }

        @Override
        @Transactional
        public OrderResponse updateOrderStatus(UUID orderId, OrderUpdateStatusRequest request, String staffId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Order not found"));

                Order.OrderStatus newStatus;
                try {
                        newStatus = Order.OrderStatus.valueOf(request.getNewStatus().toUpperCase());
                } catch (IllegalArgumentException e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Invalid status value: " + request.getNewStatus());
                }

                Order.OrderStatus currentStatus = order.getStatus();

                boolean isValidTransition = false;
                if (currentStatus == Order.OrderStatus.CONFIRMED && newStatus == Order.OrderStatus.PREPARING) {
                        isValidTransition = true;
                } else if (currentStatus == Order.OrderStatus.PREPARING && newStatus == Order.OrderStatus.COMPLETED) {
                        isValidTransition = true;
                } else if (currentStatus == Order.OrderStatus.PENDING && newStatus == Order.OrderStatus.CONFIRMED) {
                        isValidTransition = true;
                }

                if (!isValidTransition) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Invalid state transition! Cannot move from " + currentStatus + " to "
                                                        + newStatus);
                }

                order.setStatus(newStatus);
                order.setUpdatedAt(LocalDateTime.now());
                order.setUpdatedBy(staffId);
                Order savedOrder = orderRepository.save(order);
                if (newStatus == Order.OrderStatus.PREPARING) {
                        processInventoryDeduction(savedOrder);
                }
                orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                                .order(savedOrder)
                                .newStatus(newStatus)
                                .reason(request.getReason())
                                .changedBy(staffId)
                                .build());

                orderAuditLogRepository.save(OrderAuditLog.builder()
                                .order(savedOrder)
                                .entityType(OrderAuditLog.EntityType.ORDER)
                                .actionType(OrderAuditLog.ActionType.STATUS_CHANGE)
                                .changedFields("status")
                                .reason(request.getReason())
                                .changedBy(staffId)
                                .build());

                eventPublisher.publishEvent(new OrderChangedEvent(this, savedOrder.getId()));

                return mapToResponse(savedOrder);
        }

        @Override
        @Transactional
        public OrderResponse updateOrder(UUID orderId, OrderUpdateRequest request, String staffId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Order not found"));

                if (order.getStatus() != Order.OrderStatus.PENDING
                                || order.getPaymentStatus() != Order.PaymentStatus.UNPAID) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Only allow editing orders with status PENDING and payment status UNPAID.");
                }

                Set<UUID> allIds = new HashSet<>();
                request.getItems().forEach(item -> {
                        allIds.add(item.getVariantId());
                        if (item.getAddons() != null) {
                                item.getAddons().forEach(addon -> allIds.add(addon.getAddonVariantId()));
                        }
                });

                Map<UUID, ProductVariantDto> productMap = productVariantService
                                .getVariantsByIds(new ArrayList<>(allIds))
                                .stream().collect(Collectors.toMap(ProductVariantDto::getId, p -> p));

                order.getItems().clear();

                BigDecimal totalOrderAmount = BigDecimal.ZERO;

                for (OrderItemRequest itemReq : request.getItems()) {
                        ProductVariantDto variantInfo = productMap.get(itemReq.getVariantId());
                        validateProduct(variantInfo);

                        BigDecimal itemUnitPrice = variantInfo.getPrice();
                        BigDecimal itemQty = BigDecimal.valueOf(itemReq.getQuantity());
                        BigDecimal itemSubtotal = itemUnitPrice.multiply(itemQty);

                        OrderItem orderItem = OrderItem.builder()
                                        .order(order)
                                        .variantId(itemReq.getVariantId())
                                        .sku(variantInfo.getSku())
                                        .variantName(variantInfo.getName())
                                        .quantity(itemReq.getQuantity())
                                        .unitPrice(itemUnitPrice)
                                        .subtotal(itemSubtotal)
                                        .addons(new ArrayList<>())
                                        .build();

                        if (itemReq.getAddons() != null) {
                                for (OrderItemAddonRequest addonReq : itemReq.getAddons()) {
                                        ProductVariantDto addonInfo = productMap.get(addonReq.getAddonVariantId());
                                        BigDecimal addonSubtotal = addonInfo.getPrice()
                                                        .multiply(BigDecimal.valueOf(addonReq.getQuantity()))
                                                        .multiply(itemQty);

                                        orderItem.getAddons().add(OrderItemAddon.builder()
                                                        .orderItem(orderItem)
                                                        .addonVariantId(addonReq.getAddonVariantId())
                                                        .sku(addonInfo.getSku())
                                                        .addonName(addonInfo.getName())
                                                        .quantity(addonReq.getQuantity())
                                                        .unitPrice(addonInfo.getPrice())
                                                        .subtotal(addonSubtotal)
                                                        .build());
                                        itemSubtotal = itemSubtotal.add(addonSubtotal);
                                }
                        }
                        orderItem.setSubtotal(itemSubtotal);
                        order.getItems().add(orderItem);
                        totalOrderAmount = totalOrderAmount.add(itemSubtotal);
                }

                order.setTotalAmount(totalOrderAmount);
                if (request.getNotes() != null) {
                        order.setNotes(request.getNotes());
                }
                order.setUpdatedAt(LocalDateTime.now());
                order.setUpdatedBy(staffId);

                Order savedOrder = orderRepository.save(order);

                orderAuditLogRepository.save(OrderAuditLog.builder()
                                .order(savedOrder)
                                .entityType(OrderAuditLog.EntityType.ORDER)
                                .actionType(OrderAuditLog.ActionType.UPDATE)
                                .changedFields("items, total_amount, notes")
                                .reason(request.getUpdateReason() != null ? request.getUpdateReason()
                                                : "Staff updated order")
                                .changedBy(staffId)
                                .build());

                return mapToResponse(savedOrder);
        }

        @Override
        @Transactional
        public OrderResponse cancelOrderByCustomer(UUID orderId, UUID customerId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Order not found"));

                // Xác thực chủ sở hữu
                if (!order.getCustomerId().equals(customerId)) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this order");
                }

                // Ràng buộc: Chỉ PENDING và CONFIRMED mới được hủy
                if (order.getStatus() != Order.OrderStatus.PENDING
                                && order.getStatus() != Order.OrderStatus.CONFIRMED) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Cannot cancel order in status: " + order.getStatus()
                                                        + ". Preparing or Completed orders cannot be cancelled via App.");
                }

                order.setStatus(Order.OrderStatus.CANCELLED);
                boolean wasPaid = order.getPaymentStatus() == Order.PaymentStatus.PAID;

                if (wasPaid) {
                        order.setPaymentStatus(Order.PaymentStatus.REFUNDED);
                }

                order.setUpdatedAt(LocalDateTime.now());
                order.setUpdatedBy(customerId.toString());
                Order savedOrder = orderRepository.save(order);

                orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                                .order(savedOrder)
                                .newStatus(Order.OrderStatus.CANCELLED)
                                .reason("Cancelled by Customer via App")
                                .changedBy(customerId.toString())
                                .build());

                orderAuditLogRepository.save(OrderAuditLog.builder()
                                .order(savedOrder)
                                .entityType(OrderAuditLog.EntityType.ORDER)
                                .actionType(OrderAuditLog.ActionType.CANCEL_ENTIRE_ORDER)
                                .changedFields("status, payment_status")
                                .reason("Customer self-cancelled")
                                .changedBy(customerId.toString())
                                .build());

                System.out.println("[MOCK] Gọi InventoryService: Hoàn trả tồn kho cho đơn hàng " + orderId);

                if (wasPaid) {
                        System.out.println(
                                        "[MOCK] Gọi PaymentService: Kích hoạt hoàn tiền ONLINE (Refund) cho khách hàng "
                                                        + customerId);
                }

                return mapToResponse(savedOrder);
        }

        @Override
        @Transactional
        public OrderResponse cancelOrderByStaff(UUID orderId, CancelOrderRequest request, String staffId, String role) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Order not found"));

                if (order.getStatus() == Order.OrderStatus.COMPLETED
                                || order.getStatus() == Order.OrderStatus.CANCELLED) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Cannot cancel an already completed or cancelled order.");
                }

                boolean isWaste = false;

                if (order.getStatus() == Order.OrderStatus.PREPARING) {
                        if (!"MANAGER".equalsIgnoreCase(role)) {
                                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                                "Only MANAGER can cancel an order that is currently PREPARING.");
                        }
                        isWaste = true;
                }

                order.setStatus(Order.OrderStatus.CANCELLED);
                boolean wasPaid = order.getPaymentStatus() == Order.PaymentStatus.PAID;

                if (wasPaid) {
                        order.setPaymentStatus(Order.PaymentStatus.REFUNDED);
                }

                order.setUpdatedAt(LocalDateTime.now());
                order.setUpdatedBy(staffId);
                Order savedOrder = orderRepository.save(order);

                orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                                .order(savedOrder)
                                .newStatus(Order.OrderStatus.CANCELLED)
                                .reason(request.getReason())
                                .changedBy(staffId)
                                .build());

                orderAuditLogRepository.save(OrderAuditLog.builder()
                                .order(savedOrder)
                                .entityType(OrderAuditLog.EntityType.ORDER)
                                .actionType(OrderAuditLog.ActionType.CANCEL_ENTIRE_ORDER)
                                .changedFields("status, payment_status")
                                .reason(request.getReason())
                                .changedBy(staffId)
                                .build());

                if (isWaste) {
                        System.out.println("[MOCK] Gọi InventoryService: Ghi nhận HAO HỤT (WASTE) cho đơn hàng "
                                        + orderId + ". (Không cộng lại vào kho)");
                } else {
                        System.out.println("[MOCK] Gọi InventoryService: HOÀN TRẢ tồn kho cho đơn hàng " + orderId);
                }

                if (wasPaid) {
                        System.out.println(
                                        "[MOCK] Báo cáo POS: Chi tiền MẶT từ két để hoàn trả cho đơn hàng " + orderId);
                }

                return mapToResponse(savedOrder);
        }

        @Override
        public EstimatePreparationTimeResponse estimatePreparationTime(UUID orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Order not found"));

                int totalPrepTime = 0;
                for (OrderItem item : order.getItems()) {
                        // Tính thời gian cho variant chính
                        int baseTime = variantIngredientRepository.findByVariantId(item.getVariantId())
                                        .stream()
                                        .mapToInt(vi -> vi.getIngredient().getPreparationTimeMinutes())
                                        .sum();
                        int itemTime = baseTime * item.getQuantity();

                        // Tính thời gian cho addon
                        for (OrderItemAddon addon : item.getAddons()) {
                                int addonTime = variantIngredientRepository.findByVariantId(addon.getAddonVariantId())
                                                .stream()
                                                .mapToInt(vi -> vi.getIngredient().getPreparationTimeMinutes())
                                                .sum();
                                itemTime += addonTime * addon.getQuantity() * item.getQuantity();
                        }

                        item.setPreparationTimeMinutes(itemTime);
                        totalPrepTime += itemTime;
                }

                order.setEstimatedPreparationTimeMinutes(totalPrepTime);
                orderRepository.save(order);

                return EstimatePreparationTimeResponse.builder()
                                .orderId(orderId)
                                .estimatedPreparationTimeMinutes(totalPrepTime)
                                .build();
        }

        @Transactional
        public FlagOrderResponse flagOrder(UUID orderId, FlagOrderRequest request) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Order not found"));

                order.setFlagged(true);
                order.setFlagReason(request.getReason());
                order.setFlagNotes(request.getNotes());
                order.setFlaggedAt(LocalDateTime.now());

                orderRepository.save(order);

                FlagOrderResponse response = new FlagOrderResponse();
                response.setOrderId(order.getId());
                response.setFlagged(order.getFlagged());
                response.setReason(order.getFlagReason());
                response.setNotes(order.getFlagNotes());
                response.setFlaggedAt(order.getFlaggedAt());

                return response;
        }

        @Override
        @Transactional
        public Map<String, String> reassignOrder(UUID orderId, ReassignOrderRequest request, String managerId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Order not found"));

                if (order.getStatus() == Order.OrderStatus.COMPLETED) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is already completed");
                }
                if (order.getStatus() == Order.OrderStatus.CANCELLED) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is already closed");
                }

                if (request.getNewStaffId().equals(order.getAssignedStaffId())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Order is already assigned to this staff member");
                }

                order.setAssignedStaffId(request.getNewStaffId());
                order.setUpdatedAt(LocalDateTime.now());
                order.setUpdatedBy(managerId);
                Order savedOrder = orderRepository.save(order);

                orderAuditLogRepository.save(OrderAuditLog.builder()
                                .order(savedOrder)
                                .entityType(OrderAuditLog.EntityType.ORDER)
                                .actionType(OrderAuditLog.ActionType.REASSIGN)
                                .changedFields("assigned_staff_id")
                                .reason("Reassigned by manager: " + managerId)
                                .changedBy(managerId)
                                .build());

                eventPublisher.publishEvent(new OrderChangedEvent(this, savedOrder.getId()));

                return Map.of("message", "Order reassigned successfully");
        }

        @Override
        @Transactional(readOnly = true)
        public List<ExternalOrderResponse> getOrdersForReport(UUID franchiseId, LocalDate startDate,
                        LocalDate endDate) {
                LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
                LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : null;
                return orderRepository.findOrdersForReport(franchiseId, start, end).stream()
                                .map(this::mapToExternalOrderResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<ExternalOrderResponse> getAllOrdersForReport(LocalDate startDate, LocalDate endDate) {
                LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
                LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : null;
                return orderRepository.findOrdersForReport(null, start, end).stream()
                                .map(this::mapToExternalOrderResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<ExternalOrderItemResponse> getOrderItemsForReport(UUID franchiseId, LocalDate startDate,
                        LocalDate endDate) {
                LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
                LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : null;
                return orderItemRepository.findOrderItemsForReport(franchiseId, start, end).stream()
                                .map(this::mapToExternalOrderItemResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<ExternalOrderResponse> getOrdersForCustomer(UUID customerId) {
                return orderRepository.findByCustomerId(customerId).stream()
                                .map(this::mapToExternalOrderResponse)
                                .collect(Collectors.toList());
        }

        private ExternalOrderResponse mapToExternalOrderResponse(Order order) {
                return ExternalOrderResponse.builder()
                                .id(order.getId())
                                .franchiseId(order.getFranchiseId())
                                .paymentMethod(order.getPaymentStatus() != null ? order.getPaymentStatus().name()
                                                : "UNKNOWN")
                                .totalAmount(order.getTotalAmount())
                                .status(order.getStatus() != null ? order.getStatus().name() : "PENDING")
                                .createdAt(order.getCreatedAt())
                                .build();
        }

        private ExternalOrderItemResponse mapToExternalOrderItemResponse(OrderItem item) {
                return ExternalOrderItemResponse.builder()
                                .productId(item.getVariantId())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .orderStatus(item.getOrder() != null && item.getOrder().getStatus() != null
                                                ? item.getOrder().getStatus().name()
                                                : "PENDING")
                                .createdAt(item.getOrder() != null ? item.getOrder().getCreatedAt() : null)
                                .build();
        }

        private void processInventoryDeduction(Order order) {

                Map<UUID, BigDecimal> aggregatedIngredients = new HashMap<>();

                for (OrderItem item : order.getItems()) {
                        List<VariantIngredient> recipe = variantIngredientRepository
                                        .findByVariantId(item.getVariantId());

                        for (VariantIngredient vi : recipe) {
                                UUID ingId = vi.getIngredient().getId();
                                BigDecimal totalQty = vi.getQuantity().multiply(BigDecimal.valueOf(item.getQuantity()));

                                aggregatedIngredients.put(ingId, aggregatedIngredients
                                                .getOrDefault(ingId, BigDecimal.ZERO).add(totalQty));
                        }

                        for (OrderItemAddon addon : item.getAddons()) {
                                List<VariantIngredient> addonRecipe = variantIngredientRepository
                                                .findByVariantId(addon.getAddonVariantId());

                                for (VariantIngredient vi : addonRecipe) {
                                        UUID ingId = vi.getIngredient().getId();
                                        BigDecimal totalQty = vi.getQuantity()
                                                        .multiply(BigDecimal.valueOf(addon.getQuantity()))
                                                        .multiply(BigDecimal.valueOf(item.getQuantity()));

                                        aggregatedIngredients.put(ingId, aggregatedIngredients
                                                        .getOrDefault(ingId, BigDecimal.ZERO).add(totalQty));
                                }
                        }
                }

                for (Map.Entry<UUID, BigDecimal> entry : aggregatedIngredients.entrySet()) {
                        UUID ingredientId = entry.getKey();
                        BigDecimal quantityToDeduct = entry.getValue();

                        inventoryClientService.deductIngredient(ingredientId, quantityToDeduct, order.getOrderNumber());
                }
        }

}
