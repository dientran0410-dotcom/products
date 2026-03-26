package com.fsa.franchise.product_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Kafka event published to topic "notification.events" after a successful order creation.
 * Consumed by Notification Service to send order confirmation push notifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    /** Unique ID for this event (UUID string). */
    private String eventId;

    /** ID of the user who initiated the action — used as the Kafka message key. */
    private String userId;

    /** Optional: recipient address (email, phone). May be null. */
    private String recipient;

    /** Event type, e.g. ORDER_CONFIRMATION. */
    private String type;

    /** Delivery channel, e.g. PUSH, EMAIL, SMS. */
    private String channel;

    /** Arbitrary payload data (orderId, total, orderNumber, etc.). */
    private Map<String, Object> data;
}
