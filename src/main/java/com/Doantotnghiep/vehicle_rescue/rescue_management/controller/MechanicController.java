package com.Doantotnghiep.vehicle_rescue.rescue_management.controller;

import com.Doantotnghiep.vehicle_rescue.common.dto.ApiResponse;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.AddMechanicServiceRequestDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.UpdateMechanicServiceRequestDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.UpdateProfileRequestDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.*;
import com.Doantotnghiep.vehicle_rescue.rescue_management.service.MechanicProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mechanic")
public class MechanicController {
    private final MechanicProfileService mechanicService;
    @GetMapping("/profile")
    public ApiResponse<ProfileResponseDTO> getProfile() {
        ProfileResponseDTO profile = mechanicService.getProfile();
        return ApiResponse.<ProfileResponseDTO>builder()
                .success(true)
                .code(200)
                .message("Lấy hồ sơ thành công")
                .data(profile)
                .build();
    }
    @PutMapping("/profile")
    public ApiResponse<ProfileResponseDTO> updateProfile(@Valid @RequestBody UpdateProfileRequestDTO request) {
        ProfileResponseDTO profile = mechanicService.updateProfile(request);
        return ApiResponse.<ProfileResponseDTO>builder()
                .success(true)
                .code(200)
                .message("Cập nhật hồ sơ thành công")
                .data(profile)
                .build();
    }
    @PostMapping("/services")
    public ApiResponse<Void> addService(@Valid @RequestBody AddMechanicServiceRequestDTO request) {
        mechanicService.addService(request);
        return ApiResponse.<Void>builder()
                .success(true)
                .code(200)
                .message("Thêm dịch vụ thành công")
                .build();
    }
    @GetMapping("/services")
    public ApiResponse<List<MechanicServiceResponseDTO>> getServices() {
        return ApiResponse.<List<MechanicServiceResponseDTO>>builder()
                .success(true)
                .code(200)
                .data(mechanicService.getServices())
                .build();
    }
    @PutMapping("/services/{serviceId}")
    public ApiResponse<Void> updateServicePrice(
            @PathVariable UUID serviceId,
            @Valid @RequestBody UpdateMechanicServiceRequestDTO request) {
        mechanicService.updateServicePrice(serviceId, request);
        return ApiResponse.<Void>builder()
                .success(true)
                .code(200)
                .message("Cập nhật giá dịch vụ thành công")
                .build();
    }
    @GetMapping("/requested-orders")
    public ApiResponse<List<MechanicOrderItemResponse>> getRequestedOrders() {

        return ApiResponse.<List<MechanicOrderItemResponse>>builder()
                .success(true)
                .code(200)
                .data(mechanicService.getRequestedOrdersForMechanic())
                .message("Lấy danh sách được yêu cầu hỗ trợ thành công")
                .build();
    }
    @PutMapping("/order/{orderId}/accept")
    public ApiResponse<Void> acceptOrder(@PathVariable UUID orderId) {
        mechanicService.acceptOrder(orderId);
        return ApiResponse.<Void>builder()
                .success(true)
                .code(200)
                .message("Chấp nhận yêu cầu thành công")
                .build();
    }

    @PutMapping("/order/{orderId}/cancel")
    public ApiResponse<Void> cancelOrder(@PathVariable UUID orderId) {
        mechanicService.cancelOrder(orderId);
        return ApiResponse.<Void>builder()
                .success(true)
                .code(200)
                .message("Từ chối yêu cầu thành công")
                .build();
    }
    @PutMapping("/order/{orderId}/in-process")
    public ApiResponse<Void> inProcessOrder(@PathVariable UUID orderId) {
        mechanicService.inProcessOrder(orderId);
        return ApiResponse.<Void>builder()
                .success(true)
                .code(200)
                .message("Bắt đầu xử lý yêu cầu")
                .build();
    }

    @PutMapping("/order/{orderId}/complete")
    public ApiResponse<Void> completeOrder(@PathVariable UUID orderId) {
        mechanicService.completeOrder(orderId);
        return ApiResponse.<Void>builder()
                .success(true)
                .code(200)
                .message("Hoàn thành yêu cầu thành công")
                .build();
    }
    @GetMapping("/current-order")
    public ApiResponse<MechanicOrderItemResponse> getCurrentOrder() {
        return ApiResponse.<MechanicOrderItemResponse>builder()
                .success(true)
                .code(200)
                .message("Lấy yêu cầu hiện tại thành công")
                .data(mechanicService.getCurrentOrder())
                .build();
    }
    @GetMapping("/order-history")
    public ApiResponse<List<MechanicOrderHistoryResponse>> getOrderHistory() {
        return ApiResponse.<List<MechanicOrderHistoryResponse>>builder()
                .success(true)
                .code(200)
                .message("Lấy lịch sử đơn thành công")
                .data(mechanicService.getOrderHistory())
                .build();
    }
    @GetMapping("/statistic")
    public ApiResponse<MechanicStatisticResponse> getStatistic() {

        return ApiResponse.<MechanicStatisticResponse>builder()
                .success(true)
                .code(200)
                .message("Lấy thống kê thành công")
                .data(mechanicService.getStatistic())
                .build();
    }
    @GetMapping("/detail/{mechanicId}")
    public ApiResponse<MechanicDetailResponse> getMechanicDetail(
            @PathVariable UUID mechanicId) {

        return ApiResponse.<MechanicDetailResponse>builder()
                .success(true)
                .code(200)
                .message("Lấy chi tiết thợ sửa thành công")
                .data(mechanicService.getMechanicDetail(mechanicId))
                .build();
    }
}
