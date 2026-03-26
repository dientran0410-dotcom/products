package com.fsa.franchise.product_service.util;

import org.apache.commons.codec.binary.Hex;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HmacUtil {

    public static String hmacSHA256(String data, String key) {

        try {

            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");

            mac.init(secretKey);

            byte[] raw = mac.doFinal(data.getBytes());

            return Hex.encodeHexString(raw);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    // private String signHmbacSHA256(String data, String key) throws Exception {
    // Mac hmacSha256 = Mac.getInstance("HmacSHA256");
    // SecretKeySpec secretKey = new
    // SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    // hmacSha256.init(secretKey);
    // byte[] hash = hmacSha256.doFinal(data.getBytes(StandardCharsets.UTF_8));
    // StringBuilder hexString = new StringBuilder();
    // for (byte b : hash) {
    // String hex = Integer.toHexString(0xff & b);
    // if (hex.length() == 1) hexString.append('0');
    // hexString.append(hex);
    // }
    // return hexString.toString();
    // }
}
