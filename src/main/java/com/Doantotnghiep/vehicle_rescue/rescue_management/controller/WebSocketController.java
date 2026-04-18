package com.Doantotnghiep.vehicle_rescue.rescue_management.controller;

import com.Doantotnghiep.vehicle_rescue.common.exception.CustomException;
import com.Doantotnghiep.vehicle_rescue.common.exception.ErrorCode;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.LocationMessage;
import com.Doantotnghiep.vehicle_rescue.rescue_management.service.MechanicProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final MechanicProfileService mechanicService;
    @MessageMapping("/mechanic/location")
    public void updateLocation(LocationMessage message,
                               SimpMessageHeaderAccessor headerAccessor) {
        Principal principal = headerAccessor.getUser();
        System.out.println("Accessor user: " + principal);

        if (principal == null) {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        mechanicService.updateLocationRealtime(message, principal);
    }
}
