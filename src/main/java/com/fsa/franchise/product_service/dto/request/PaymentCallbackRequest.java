package com.fsa.franchise.product_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentCallbackRequest {
    private String transactionId;
    private String status;
}