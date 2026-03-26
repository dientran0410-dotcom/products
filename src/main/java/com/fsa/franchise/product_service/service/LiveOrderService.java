package com.fsa.franchise.product_service.service;

import com.fsa.franchise.product_service.dto.response.LiveOrderDto;
import com.fsa.franchise.product_service.dto.response.LiveOrderResponse;
import com.fsa.franchise.product_service.entity.Order;
import com.fsa.franchise.product_service.event.OrderChangedEvent;
import com.fsa.franchise.product_service.repository.OrderRepository;
import com.fsa.franchise.product_service.specification.LiveOrderSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.scheduling.annotation.Async;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LiveOrderService {


    private final OrderRepository orderRepository;
    private final TransactionTemplate transactionTemplate;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public LiveOrderService(OrderRepository orderRepository, PlatformTransactionManager transactionManager) {
        this.orderRepository = orderRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }



    public SseEmitter connect() {
        // Create an emitter with a reasonable timeout or keep it indefinite (e.g., 30 minutes)
        SseEmitter emitter = new SseEmitter(1800000L);
        this.emitters.add(emitter);

        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            this.emitters.remove(emitter);
        });
        emitter.onError((e) -> {
            emitter.complete();
            this.emitters.remove(emitter);
        });

        // Send immediately on connect
        pushInitialData(emitter);

        return emitter;
    }

    private void pushInitialData(SseEmitter emitter) {
        try {
            // Send connection established event first instead of full data
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("SSE connection established"));
            log.info("SSE connection established for emitter {}", emitter.hashCode());
        } catch (Exception e) {
            log.error("Failed to send connection event to emitter {}", emitter.hashCode(), e);
            emitter.completeWithError(e);
            this.emitters.remove(emitter);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderChangedEvent(OrderChangedEvent event) {
        if (emitters.isEmpty()) {
            return;
        }

        log.info("Handling OrderChangedEvent for order ID: {}", event.getOrderId());
        
        LiveOrderResponse response;
        try {
            response = getActiveOrdersResponse();
        } catch (Exception e) {
            log.error("Error retrieving active orders after event for order {}", event.getOrderId(), e);
            return; // Don't crash the event listener
        }
        
        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("live-orders").data(response));
            } catch (Exception e) {
                log.warn("Failed to send update to emitter {}, marking as dead", emitter.hashCode());
                deadEmitters.add(emitter);
            }
        }
        
        if (!deadEmitters.isEmpty()) {
            this.emitters.removeAll(deadEmitters);
            log.info("Removed {} dead emitters", deadEmitters.size());
        }
    }

    public LiveOrderResponse getActiveOrdersResponse() {
        return transactionTemplate.execute(status -> {
            try {
                List<Order> activeOrders = orderRepository.findAll(
                        LiveOrderSpecification.getActiveOrders(),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                );
                log.info("Found {} active orders for live stream", activeOrders.size());

                if (activeOrders.isEmpty()) {
                    return LiveOrderResponse.builder()
                            .message("No active orders at the moment.")
                            .orders(new ArrayList<>())
                            .build();
                }

                // Mapping Must happen inside transaction to avoid LazyInitializationException
                List<LiveOrderDto> dtos = activeOrders.stream()
                        .map(this::mapToLiveOrderDto)
                        .collect(Collectors.toList());

                return LiveOrderResponse.builder()
                        .message("Fetched " + dtos.size() + " active orders.")
                        .orders(dtos)
                        .build();
            } catch (Exception e) {
                log.error("CRITICAL: Internal error in getActiveOrdersResponse during DB fetch or mapping", e);
                // Return a safe empty response instead of throwing to avoid breaking SSE connection if possible
                return LiveOrderResponse.builder()
                        .message("Error retrieving orders: " + e.getMessage())
                        .orders(new ArrayList<>())
                        .build();
            }
        });
    }


    private LiveOrderDto mapToLiveOrderDto(Order order) {
        if (order == null) return null;

        // Map Status
        String mappedStatus = "ORDERED";
        if (order.getStatus() != null) {
            mappedStatus = switch (order.getStatus()) {
                case PENDING, CONFIRMED -> "ORDERED";
                case PREPARING -> "PREPARING";
                case COMPLETED -> "COMPLETED";
                default -> order.getStatus().name();
            };
        }

        // Customer Name
        String cName = "Anonymous";
        if (order.getCustomerId() != null) {
            cName = order.getCustomerId().toString();
        }

        List<LiveOrderDto.LiveOrderItemDto> itemDtos = new ArrayList<>();
        if (order.getItems() != null) {
            try {
                order.getItems().forEach(item -> {
                    if (item == null) return;
                    
                    List<String> addOnNames = new ArrayList<>();
                    if (item.getAddons() != null) {
                        item.getAddons().forEach(addon -> {
                            if (addon == null) return;
                            String addonName = addon.getAddonName() != null ? addon.getAddonName() : "Unknown Addon";
                            int qty = addon.getQuantity() != null ? addon.getQuantity() : 1;
                            addOnNames.add(qty + "x " + addonName);
                        });
                    }
                    
                    String vName = item.getVariantName() != null ? item.getVariantName() : "Unknown Product";
                    int itemQty = item.getQuantity() != null ? item.getQuantity() : 1;

                    itemDtos.add(LiveOrderDto.LiveOrderItemDto.builder()
                            .productName(vName)
                            .quantity(itemQty)
                            .notes(item.getNotes())
                            .addons(addOnNames)
                            .build());
                });
            } catch (Exception e) {
                log.warn("Error mapping items for order {}: {}", order.getId(), e.getMessage());
            }
        }

        return LiveOrderDto.builder()
                .orderId(order.getId())
                .customerName(cName)
                .items(itemDtos)
                .currentStatus(mappedStatus)
                .createdTime(order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now())
                .build();
    }
}
