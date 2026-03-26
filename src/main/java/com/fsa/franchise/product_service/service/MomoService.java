package com.fsa.franchise.product_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsa.franchise.product_service.client.MomoApi;
import com.fsa.franchise.product_service.client.OrderClient;
import com.fsa.franchise.product_service.client.PointClient;
import com.fsa.franchise.product_service.dto.request.AddPointRequest;
import com.fsa.franchise.product_service.dto.request.MomoIpnRequest;
import com.fsa.franchise.product_service.dto.response.PaymentResponse;
import com.fsa.franchise.product_service.entity.Invoice;
import com.fsa.franchise.product_service.entity.PaymentTransaction;
import com.fsa.franchise.product_service.model.CreateMomoRequest;
import com.fsa.franchise.product_service.model.CreateMomoResponse;
import com.fsa.franchise.product_service.repository.InvoiceRepository;
import com.fsa.franchise.product_service.repository.PaymentTransactionRepository;
import com.fsa.franchise.product_service.util.HmacUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MomoService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PointClient pointClient;

    @Value("${momo.endpoint}")
    private String endpoint;

    @Value("${momo.partnerCode}")
    private String partnerCode;

    @Value("${momo.accessKey}")
    private String accessKey;

    @Value("${momo.secretKey}")
    private String secretKey;

    @Value("${momo.redirectUrl}")
    private String redirectUrl;

    @Value("${momo.ipnUrl}")
    private String ipnUrl;

    public CreateMomoResponse create(CreateMomoRequest req) throws Exception {

        String momoOrderId = UUID.randomUUID().toString();
        String requestId = UUID.randomUUID().toString();

        String orderInfo = "Pay with MoMo";
        String requestType = "captureWallet";
        String extraData = "";

        String rawHash = "accessKey=" + accessKey +
                "&amount=" + req.getAmount() +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + momoOrderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        String signature = HmacUtil.hmacSHA256(rawHash, secretKey);

        Map<String, Object> body = new HashMap<>();
        body.put("partnerCode", partnerCode);
        body.put("accessKey", accessKey);
        body.put("requestId", requestId);
        body.put("amount", req.getAmount());
        body.put("orderId", momoOrderId);
        body.put("orderInfo", orderInfo);
        body.put("redirectUrl", redirectUrl);
        body.put("ipnUrl", ipnUrl);
        body.put("extraData", extraData);
        body.put("requestType", requestType);
        body.put("signature", signature);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, request, Map.class);

        Map data = response.getBody();

        if (data == null || !"0".equals(String.valueOf(data.get("resultCode")))) {
            throw new RuntimeException("MoMo create failed: " + data);
        }

        log.info("MoMo response: {}", data);

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrderId(req.getOrderId()); // invoiceId
        transaction.setMomoOrderId(momoOrderId);
        transaction.setAmount(req.getAmount());
        transaction.setStatus("PENDING");
        transaction.setPaymentMethod("MOMO");

        paymentTransactionRepository.save(transaction);

        CreateMomoResponse momoResponse = new CreateMomoResponse();

        momoResponse.setPayUrl(
                data.get("payUrl") != null ? data.get("payUrl").toString() : null);

        momoResponse.setQrCodeUrl(
                data.get("qrCodeUrl") != null ? data.get("qrCodeUrl").toString() : null);

        return momoResponse;
    }

    public void handleIpn(MomoIpnRequest data) {

        log.info("IPN received: {}", data);

        String extraData = data.getExtraData() == null ? "" : data.getExtraData();

        String rawHash = "accessKey=" + accessKey +
            "&amount=" + data.getAmount() +
            "&extraData=" + extraData +
            "&message=" + data.getMessage() +
            "&orderId=" + data.getOrderId() +
            "&orderInfo=" + data.getOrderInfo() +
            "&orderType=" + data.getOrderType() +
            "&partnerCode=" + data.getPartnerCode() +
            "&payType=" + data.getPayType() +
            "&requestId=" + data.getRequestId() +
            "&responseTime=" + data.getResponseTime() +
            "&resultCode=" + data.getResultCode() +
            "&transId=" + data.getTransId();

        String sign = HmacUtil.hmacSHA256(rawHash, secretKey);

        log.info("RawHash: {}", rawHash);
        log.info("Generate sign: {}", sign);
        log.info("Momo sign: {}", data.getSignature());

        if (data.getSignature() == null || !sign.equals(data.getSignature())) {
            throw new RuntimeException("Invalid signature");
        }

        String momoOrderId = data.getOrderId();

        PaymentTransaction transaction = paymentTransactionRepository
            .findByMomoOrderId(momoOrderId)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if ("SUCCESS".equals(transaction.getStatus())) {
             log.info("Transaction already SUCCESS: {}", momoOrderId);
        return;
        }

        UUID invoiceId = transaction.getOrderId();

        Invoice invoice = invoiceRepository.findById(transaction.getOrderId())
            .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (data.getResultCode() == 0) {

        transaction.setStatus("SUCCESS");
        transaction.setMomoTransId(String.valueOf(data.getTransId()));

        invoice.setStatus(Invoice.InvoiceStatus.PAID);

            long points = invoice.getTotalAmount().longValue() / 1000;

            AddPointRequest request = new AddPointRequest();
            request.setCustomerId(invoice.getCustomerId());
            request.setPoints(points);

            pointClient.addPoints(request);

            log.info("Payment SUCCESS - added {} points", points);

            } else {
                transaction.setStatus("FAILED");
                invoice.setStatus(Invoice.InvoiceStatus.FAILED);
                log.info("Payment FAILED - invoiceId: {}", invoiceId);
        }

        paymentTransactionRepository.save(transaction);
        invoiceRepository.save(invoice);
    }

    public PaymentResponse handleReturn(Map<String, String> params) {

        String resultCode = params.get("resultCode");
        String momoOrderId = params.get("orderId");

        if (momoOrderId == null) {
            return new PaymentResponse(null, PaymentResponse.PaymentStatus.FAILED, "Invalid order");
        }

        PaymentTransaction transaction = paymentTransactionRepository
                .findByMomoOrderId(momoOrderId)
                .orElse(null);

        if (transaction == null) {
            return new PaymentResponse(null, PaymentResponse.PaymentStatus.FAILED, "Transaction not found");
        }

        UUID invoiceId = transaction.getOrderId();

        if ("0".equals(resultCode)) {
            return new PaymentResponse(
                    invoiceId,
                    PaymentResponse.PaymentStatus.SUCCESS,
                    "Payment success"
            );
        } else {
            return new PaymentResponse(
                    invoiceId,
                    PaymentResponse.PaymentStatus.FAILED,
                    "Payment failed"
            );
        }
    }


}
