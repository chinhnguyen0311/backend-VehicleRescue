package com.Doantotnghiep.vehicle_rescue.rescue_management.controller;

import com.Doantotnghiep.vehicle_rescue.common.exception.CustomException;
import com.Doantotnghiep.vehicle_rescue.common.exception.ErrorCode;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.LocationMessage;
import com.Doantotnghiep.vehicle_rescue.rescue_management.service.MechanicProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mechanic")
public class WebSocketController {
    private final MechanicProfileService mechanicService;
    @PostMapping("/location")
    public ResponseEntity<Void> updateLocationHttp(
            @RequestBody LocationMessage message,
            Principal principal) {
        if (principal == null) {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
        mechanicService.updateLocationRealtime(message, principal);
        return ResponseEntity.ok().build();
    }
}
