package com.fsa.franchise.product_service.service;

import com.fsa.franchise.product_service.dto.request.InventoryDeductRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryClientService {

    private final WebClient.Builder webClientBuilder;

    private static final String INVENTORY_SERVICE_NAME = "http://warehouse-inventory-supplier-service";

    public void deductIngredient(UUID ingredientId, BigDecimal quantity, String orderNumber) {
        InventoryDeductRequest request = InventoryDeductRequest.builder()
                .quantityToDeduct(quantity)
                .reason("Order " + orderNumber + " started preparing")
                .performedBy("product-service")
                .build();

        try {

            webClientBuilder.build().put()
                    .uri(INVENTORY_SERVICE_NAME + "/api/inventory-service/ingredients/{ingredientId}/deduct",
                            ingredientId)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("Thành công: Đã ra lệnh trừ {} cho nguyên liệu ID: {} của đơn {}", quantity, ingredientId,
                    orderNumber);
        } catch (Exception e) {
            log.error("Thất bại: Lỗi gọi sang Inventory Service trừ nguyên liệu {} cho đơn {}", ingredientId,
                    orderNumber, e);
        }
    }
}