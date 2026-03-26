package com.fsa.franchise.product_service.event;

import com.fsa.franchise.product_service.service.NotificationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Listens for OrderCreatedEvent and forwards it to Kafka AFTER the database
 * transaction has committed.
 *
 * <p>Using {@code TransactionPhase.AFTER_COMMIT} guarantees that the Kafka
 * notification is NEVER sent if the transaction rolls back — preventing ghost
 * order confirmations for orders that don't exist in the database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationProducer notificationProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("[NotificationEventListener] TX committed — dispatching Kafka event for orderId={}",
                event.getOrderId());

        Map<String, Object> data = new HashMap<>();
        data.put("orderId",     event.getOrderId());
        data.put("orderNumber", event.getOrderNumber());
        data.put("total",       event.getTotal());

        notificationProducer.sendNotification(
                event.getCustomerId(),
                "ORDER_CONFIRMATION",
                "PUSH",
                data
        );
    }
}
