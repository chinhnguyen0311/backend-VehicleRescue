package com.Doantotnghiep.vehicle_rescue.rescue_management.controller;

import com.Doantotnghiep.vehicle_rescue.common.dto.ApiResponse;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.AddMechanicServiceRequestDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.UpdateMechanicServiceRequestDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.UpdateProfileRequestDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.MechanicServiceResponseDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.ProfileResponseDTO;
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
}
