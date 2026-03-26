package com.fsa.franchise.product_service.service.serviceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fsa.franchise.product_service.dto.request.ProductPerformanceRequest;
import com.fsa.franchise.product_service.dto.response.ExternalOrderItemResponse;
import com.fsa.franchise.product_service.dto.response.ExternalOrderResponse;
import com.fsa.franchise.product_service.entity.Order;
import com.fsa.franchise.product_service.entity.OrderItem;
import com.fsa.franchise.product_service.repository.OrderItemRepository;
import com.fsa.franchise.product_service.repository.OrderRepository;
import com.fsa.franchise.product_service.service.NotificationProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private NotificationProducer notificationProducer;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID franchiseId;
    private UUID customerId;
    private UUID orderId;
    private UUID orderItemId;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        franchiseId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        orderItemId = UUID.randomUUID();
        startDate = LocalDate.now().minusDays(7);
        endDate = LocalDate.now();
    }

    private Order createMockOrder() {
        Order order = new Order();
        order.setId(orderId);
        order.setFranchiseId(franchiseId);
        order.setCustomerId(customerId);
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }

    private OrderItem createMockOrderItem() {
        OrderItem item = new OrderItem();
        item.setId(orderItemId);
        item.setVariantId(UUID.randomUUID());
        item.setQuantity(2);
        return item;
    }

    @Test
    void testGetOrdersForReport() {
        Order order = createMockOrder();
        when(orderRepository.findOrdersForReport(eq(franchiseId), any(), any()))
                .thenReturn(List.of(order));

        List<ExternalOrderResponse> result = orderService.getOrdersForReport(franchiseId, startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(orderId, result.get(0).getId());
        assertEquals(franchiseId, result.get(0).getFranchiseId());
        verify(orderRepository).findOrdersForReport(eq(franchiseId), any(), any());
    }

    @Test
    void testGetAllOrdersForReport() {
        Order order = createMockOrder();
        when(orderRepository.findOrdersForReport(isNull(), any(), any()))
                .thenReturn(List.of(order));

        List<ExternalOrderResponse> result = orderService.getAllOrdersForReport(startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(orderId, result.get(0).getId());
        verify(orderRepository).findOrdersForReport(isNull(), any(), any());
    }

    @Test
    void testGetOrderItemsForReport() {
        OrderItem item = createMockOrderItem();
        item.setOrder(createMockOrder()); // Avoid NullPointerException in mapping
        
        when(orderItemRepository.findOrderItemsForReport(eq(franchiseId), any(), any()))
                .thenReturn(List.of(item));

        List<ExternalOrderItemResponse> result = orderService.getOrderItemsForReport(franchiseId, startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(item.getVariantId(), result.get(0).getProductId());
        verify(orderItemRepository).findOrderItemsForReport(eq(franchiseId), any(), any());
    }

    @Test
    void testGetOrdersForCustomer() {
        Order order = createMockOrder();
        when(orderRepository.findByCustomerId(customerId)).thenReturn(List.of(order));

        List<ExternalOrderResponse> result = orderService.getOrdersForCustomer(customerId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(orderId, result.get(0).getId());
        verify(orderRepository).findByCustomerId(customerId);
    }
}
