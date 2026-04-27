package com.Doantotnghiep.vehicle_rescue.system.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FcmTokenCacheService {
    private final Map<UUID, String> orderTokenMap = new ConcurrentHashMap<>();

    public void saveTokenForOrder(UUID orderId, String fcmToken) {
        if (fcmToken != null && !fcmToken.isEmpty()) {
            orderTokenMap.put(orderId, fcmToken);
        }
    }

    public String getTokenByOrderId(UUID orderId) {
        return orderTokenMap.get(orderId);
    }

    public void removeToken(UUID orderId) {
        orderTokenMap.remove(orderId);
    }
}
