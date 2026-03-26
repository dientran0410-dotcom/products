package com.fsa.franchise.product_service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)

public class CreateMomoResponse {
    // private String partnerCode;
    // private String requestId;
    // private String orderId;
    // private long amount;
    // private long responseTime;
    // private String message;
    // private int resultCode;
    // private String payUrl;
    // private String deepLink;
    // private String qrCodeUrl;

    private String payUrl;
    private String qrCodeUrl;
    private String deepLink;

}
