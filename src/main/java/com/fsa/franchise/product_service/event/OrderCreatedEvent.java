package com.fsa.franchise.product_service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

/**
 * Internal Spring event published inside createOrder() after orderRepository.save().
 * Carries the notification payload so the AFTER_COMMIT listener can send to Kafka
 * without re-querying the database.
 */
@Getter
public class OrderCreatedEvent extends ApplicationEvent {

    private final String orderId;
    private final String orderNumber;
    private final BigDecimal total;
    private final String customerId;

    public OrderCreatedEvent(Object source,
                             String orderId,
                             String orderNumber,
                             BigDecimal total,
                             String customerId) {
        super(source);
        this.orderId     = orderId;
        this.orderNumber = orderNumber;
        this.total       = total;
        this.customerId  = customerId;
    }
}
