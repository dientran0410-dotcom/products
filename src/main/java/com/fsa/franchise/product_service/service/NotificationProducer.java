package com.fsa.franchise.product_service.service;

import com.fsa.franchise.product_service.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer that publishes NotificationEvent to topic "notification.events".
 *
 * <p>Sending is fire-and-forget (non-blocking). Kafka failures are caught and logged
 * so they NEVER propagate to the calling order-creation flow.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private static final String TOPIC = "notification.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publishes an order notification event to Kafka asynchronously.
     *
     * @param userId  ID of the customer — used as the Kafka message key
     * @param type    Event type, e.g. {@code ORDER_CONFIRMATION}
     * @param channel Delivery channel, e.g. {@code PUSH}
     * @param data    Arbitrary payload (orderId, total, orderNumber …)
     */
    public void sendNotification(String userId,
                                 String type,
                                 String channel,
                                 Map<String, Object> data) {
        try {
            NotificationEvent event = NotificationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .userId(userId)
                    .type(type)
                    .channel(channel)
                    .data(data)
                    .build();

            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(TOPIC, userId, event);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.warn("[NotificationProducer] Failed to send event to topic '{}' for userId={}: {}",
                            TOPIC, userId, ex.getMessage());
                } else {
                    log.info("[NotificationProducer] Event sent → topic='{}', partition={}, offset={}, userId={}",
                            TOPIC,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            userId);
                }
            });

        } catch (Exception ex) {
            // Swallow all exceptions — Kafka must never break order creation
            log.warn("[NotificationProducer] Unexpected error building/sending notification event for userId={}: {}",
                    userId, ex.getMessage());
        }
    }
}
