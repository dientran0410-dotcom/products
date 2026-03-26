package com.fsa.franchise.product_service.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class MomoIpnRequest {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private String extraData;
    private String signature;
    private Long amount;
    private String orderInfo;
    private String orderType;
    private Long transId;
    private Integer resultCode;
    private String message;
    private String payType;
    private Long responseTime;
}
