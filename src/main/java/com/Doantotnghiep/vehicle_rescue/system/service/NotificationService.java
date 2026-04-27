package com.Doantotnghiep.vehicle_rescue.system.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class NotificationService {
    public void sendPushNotification(String targetToken, String title, String body) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(targetToken)
                    .setNotification(notification)
                    .putData("type", "ORDER_ACCEPTED")
                    .build();

            // Gửi thông báo
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Đã gửi thông báo thành công. Response FCM: {}", response);
        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo FCM", e);
        }
    }
}
