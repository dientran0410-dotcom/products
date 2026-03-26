package com.fsa.franchise.product_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsa.franchise.product_service.dto.request.ProductPerformanceRequest;
import com.fsa.franchise.product_service.dto.response.ExternalOrderItemResponse;
import com.fsa.franchise.product_service.dto.response.ExternalOrderResponse;
import com.fsa.franchise.product_service.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class InternalOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID franchiseId;
    private UUID customerId;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        franchiseId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        startDate = LocalDate.now().minusDays(7);
        endDate = LocalDate.now();
    }

    @Test
    void testGetOrdersForReport() throws Exception {
        ExternalOrderResponse response = ExternalOrderResponse.builder()
                .id(UUID.randomUUID())
                .franchiseId(franchiseId)
                .createdAt(LocalDateTime.now())
                .build();

        when(orderService.getOrdersForReport(eq(franchiseId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/products/internal/orders/report")
                        .param("franchiseId", franchiseId.toString())
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].franchiseId").value(franchiseId.toString()));

        verify(orderService).getOrdersForReport(eq(franchiseId), eq(startDate), eq(endDate));
    }

    @Test
    void testGetAllOrdersForReport() throws Exception {
        ExternalOrderResponse response = ExternalOrderResponse.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .build();

        when(orderService.getAllOrdersForReport(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/products/internal/orders/report/all")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());

        verify(orderService).getAllOrdersForReport(eq(startDate), eq(endDate));
    }

    @Test
    void testGetOrderItemsForReport() throws Exception {
        ExternalOrderItemResponse response = ExternalOrderItemResponse.builder()
                .productId(UUID.randomUUID())
                .quantity(5)
                .unitPrice(java.math.BigDecimal.valueOf(10.0))
                .build();

        when(orderService.getOrderItemsForReport(any(), any(), any()))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/products/internal/orders/report/items")
                        .param("franchiseId", franchiseId.toString())
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").exists())
                .andExpect(jsonPath("$[0].quantity").value(5));

        verify(orderService).getOrderItemsForReport(any(), any(), any());
    }

    @Test
    void testGetOrdersForCustomer() throws Exception {
        ExternalOrderResponse response = ExternalOrderResponse.builder()
                .id(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .build();

        when(orderService.getOrdersForCustomer(eq(customerId)))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/products/internal/orders/customer")
                        .param("customerId", customerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());

        verify(orderService).getOrdersForCustomer(eq(customerId));
    }
}
