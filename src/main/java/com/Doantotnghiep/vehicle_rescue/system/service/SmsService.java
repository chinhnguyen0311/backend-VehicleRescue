//package com.Doantotnghiep.vehicle_rescue.system.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Base64;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class SmsService {
//    @Value("${vonage.api-key}")
//    private String apiKey;
//
//    @Value("${vonage.api-secret}")
//    private String apiSecret;
//
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    public void sendSms(String phone, String message) {
//        // 1. Format số điện thoại chuẩn quốc tế cho Vonage (vd: 84987654321)
//        String formattedPhone = phone.trim();
//        if (formattedPhone.startsWith("0")) {
//            formattedPhone = "84" + formattedPhone.substring(1);
//        } else if (formattedPhone.startsWith("+84")) {
//            formattedPhone = "84" + formattedPhone.substring(3);
//        }
//
//        String url = "https://rest.nexmo.com/sms/json";
//
//        // 2. Tạo Body Request
//        Map<String, Object> body = new HashMap<>();
//        body.put("api_key", apiKey);
//        body.put("api_secret", apiSecret);
//        body.put("to", formattedPhone);
//
//        // Sender mặc định cho tài khoản Trial thường là chữ "Vonage APIs"
//        body.put("from", "Vonage APIs");
//        body.put("type", "unicode");
//        // Vẫn nên gửi tiếng Việt không dấu để đảm bảo tin nhắn không bị vỡ font
//        body.put("text", message);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
//
//        try {
//            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
//            System.out.println("=== SMS RESPONSE TỪ VONAGE ===");
//            System.out.println(response.getBody());
//        } catch (Exception e) {
//            System.err.println("Lỗi gọi API Vonage: " + e.getMessage());
//        }
//    }
//
//}
