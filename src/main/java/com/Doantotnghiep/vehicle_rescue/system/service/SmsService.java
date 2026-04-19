package com.Doantotnghiep.vehicle_rescue.system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SmsService {
    @Value("${esms.api-key}")
    private String apiKey;

    @Value("${esms.secret-key}")
    private String secretKey;

    @Value("${esms.brandname}")
    private String brandname;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendSms(String phone, String message) {
        // 1. Dọn dẹp số điện thoại (Xóa khoảng trắng, thêm số 0 nếu cần)
        String formattedPhone = phone.trim();
        if (formattedPhone.startsWith("+84")) {
            formattedPhone = "0" + formattedPhone.substring(3);
        }

        String url = "https://rest.esms.vn/MainService.svc/json/SendMultipleMessage_V4_post_json/";

        Map<String, Object> body = new HashMap<>();
        body.put("Phone", formattedPhone);
        body.put("Content", message); // Bắt buộc text thuần (không Emoji)
        body.put("ApiKey", apiKey);
        body.put("SecretKey", secretKey);

        // 🔥 ĐÃ SỬA 1: Tạm thời vô hiệu hóa Brandname vì tài khoản chưa được duyệt (Lỗi 104)
        // Nếu eSMS bắt buộc phải có key này đối với loại 8, hãy điền thử: body.put("Brandname", "Verify");
        // Còn thường thì chúng ta chỉ cần comment nó lại:
        // body.put("Brandname", brandname);

        // 🔥 ĐÃ SỬA 2: Đổi SmsType từ 2 thành 8 (Loại tin nhắn chăm sóc khách hàng bằng đầu số chung)
        body.put("SmsType", 8);

        // Bỏ comment dòng dưới nếu bạn truyền chuỗi tiếng Việt CÓ DẤU
        // body.put("IsUnicode", "1");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            System.out.println("=== SMS RESPONSE TRẢ VỀ TỪ ESMS ===");
            System.out.println(response.getBody());
        } catch (Exception e) {
            System.err.println("Lỗi gọi API eSMS: " + e.getMessage());
        }
    }

}
