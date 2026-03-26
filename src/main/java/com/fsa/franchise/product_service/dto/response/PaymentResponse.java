package com.fsa.franchise.product_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private UUID invoiceId;
    private PaymentStatus status;
    private String message;

    public enum PaymentStatus {
        SUCCESS,
        FAILED,
        PENDING
    }

}
