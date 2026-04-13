package com.Doantotnghiep.vehicle_rescue.rescue_management.controller;

import com.Doantotnghiep.vehicle_rescue.common.dto.ApiResponse;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.CreateRescueOrderRequest;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.MechanicSearchResultDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.SearchMechanicRequestDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.RescueOrderResponse;
import com.Doantotnghiep.vehicle_rescue.rescue_management.service.RescueOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class RescueOrderController {
    private final RescueOrderService rescueOrderService;

    @PostMapping("/search")
    public ApiResponse<List<MechanicSearchResultDTO>> searchNearbyMechanics(
            @RequestBody SearchMechanicRequestDTO request
    ) {
        List<MechanicSearchResultDTO> result =
                rescueOrderService.searchNearbyMechanics(request);

        return ApiResponse.<List<MechanicSearchResultDTO>>builder()
                .success(true)
                .code(200)
                .data(result)
                .message("Tìm kiếm thợ sửa xe thành công")
                .build();
    }
    @PostMapping
    public ApiResponse<RescueOrderResponse> createOrder(@RequestBody CreateRescueOrderRequest request) {
        RescueOrderResponse order = rescueOrderService.createRescueOrder(request);

        return ApiResponse.<RescueOrderResponse>builder()
                .success(true)
                .code(200)
                .message("Gửi yêu cầu cứu hộ thành công")
                .data(order)
                .build();
    }
}
