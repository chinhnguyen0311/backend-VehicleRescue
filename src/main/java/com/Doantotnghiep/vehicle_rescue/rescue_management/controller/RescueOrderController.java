package com.Doantotnghiep.vehicle_rescue.rescue_management.controller;

import com.Doantotnghiep.vehicle_rescue.common.dto.ApiResponse;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.CreateRescueOrderRequest;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.CustomerOrderResponse;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.MechanicSearchResultDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.SearchMechanicRequestDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.RescueOrderResponse;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.ServiceResponseDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.service.RescueOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class RescueOrderController {
    private final RescueOrderService rescueOrderService;
    @GetMapping("/services")
    public ApiResponse<List<ServiceResponseDTO>> getAllServices() {
        return ApiResponse.<List<ServiceResponseDTO>>builder()
                .success(true)
                .code(200)
                .message("Lấy danh sách dịch vụ thành công")
                .data(rescueOrderService.getAllServices())
                .build();
    }
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
    @GetMapping("/my")
    public ApiResponse<List<CustomerOrderResponse>> getOrders(
            @RequestParam String phone) {

        return ApiResponse.<List<CustomerOrderResponse>>builder()
                .success(true)
                .code(200)
                .message("Lấy đơn đang hoạt động thành công")
                .data(rescueOrderService.getMyOrders(phone))
                .build();
    }
}
