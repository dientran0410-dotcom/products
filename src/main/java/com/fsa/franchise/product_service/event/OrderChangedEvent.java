package com.fsa.franchise.product_service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class OrderChangedEvent extends ApplicationEvent {
    private final UUID orderId;

    public OrderChangedEvent(Object source, UUID orderId) {
        super(source);
        this.orderId = orderId;
    }
}
