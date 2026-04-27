package com.Doantotnghiep.vehicle_rescue.system.controller;

import com.Doantotnghiep.vehicle_rescue.system.service.FcmTokenCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private FcmTokenCacheService tokenCacheService;

    // API này Khách hàng sẽ gọi sau khi tạo đơn cứu hộ xong
    @PostMapping("/save-token")
    public ResponseEntity<String> saveToken(
            @RequestParam UUID orderId,
            @RequestParam String token) {

        tokenCacheService.saveTokenForOrder(orderId, token);
        return ResponseEntity.ok("Đã lưu token tạm thời cho đơn: " + orderId);
    }
}
