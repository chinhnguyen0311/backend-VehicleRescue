package com.Doantotnghiep.vehicle_rescue.system.controller;

import com.Doantotnghiep.vehicle_rescue.common.dto.ApiResponse;
import com.Doantotnghiep.vehicle_rescue.system.dto.response.AdminDashboardResponse;
import com.Doantotnghiep.vehicle_rescue.system.dto.response.MechanicAdminResponse;
import com.Doantotnghiep.vehicle_rescue.system.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponse> getDashBoard() {
        return ApiResponse.<AdminDashboardResponse>builder()
                .success(true)
                .code(200)
                .message("Lấy thống kê thành công")
                .data(adminService.getDashboard())
                .build();
    }
    @GetMapping("/mechanics")
    public ApiResponse<List<MechanicAdminResponse>> getMechanics() {
        return ApiResponse.<List<MechanicAdminResponse>>builder()
                .success(true)
                .code(200)
                .message("Lấy danh sách thợ máy thành công")
                .data(adminService.getAllMechanicsForAdmin())
                .build();
    }
}
