package com.Doantotnghiep.vehicle_rescue.system.controller;

import com.Doantotnghiep.vehicle_rescue.common.dto.ApiResponse;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.SubscriptionStatus;
import com.Doantotnghiep.vehicle_rescue.system.dto.response.*;
import com.Doantotnghiep.vehicle_rescue.system.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    @GetMapping("/pending-accounts")
    public ApiResponse<List<PendingAccountResponse>> getPendingAccounts() {
        return ApiResponse.<List<PendingAccountResponse>>builder()
                .success(true)
                .code(200)
                .message("Lấy danh sách tài khoản đang chờ duyệt thành công")
                .data(adminService.getPendingAccounts())
                .build();
    }
    @PutMapping("/pending-accounts/{accountId}/approve")
    public ApiResponse<Void> approveAccount(@PathVariable UUID accountId) {
        adminService.approveAccount(accountId);
        return ApiResponse.<Void>builder()
                .success(true)
                .code(200)
                .message("Duyệt tài khoản thành công")
                .build();
    }

     @PutMapping("/pending-accounts/{accountId}/reject")
    public ApiResponse<Void> rejectAccount(@PathVariable UUID accountId) {
         adminService.rejectAccount(accountId);
         return ApiResponse.<Void>builder()
                 .success(true)
                 .code(200)
                 .message("Từ chối tài khoản thành công")
                 .build();
    }

    @GetMapping("/subscriptions")
    public ApiResponse<List<SubscriptionResponse>> getSubscriptions() {
        return ApiResponse.<List<SubscriptionResponse>>builder()
                .success(true)
                .code(200)
                .message("Lấy danh sách gia hạn đang chờ duyệt thành công")
                .data(adminService.getPendingSubscriptions())
                .build();
    }
    @PutMapping("/subscriptions/{subscriptionId}/approve")
    public ApiResponse<Void> approveSubscription(@PathVariable UUID subscriptionId) {
        adminService.approveSubscription(subscriptionId);
        return ApiResponse.<Void>builder()
                .success(true)
                .code(200)
                .message("Duyệt gia hạn thành công")
                .build();
    }
    @PutMapping("/subscriptions/{subscriptionId}/reject")
    public ApiResponse<Void> rejectSubscription(@PathVariable UUID subscriptionId) {
        adminService.rejectSubscription(subscriptionId);
        return ApiResponse.<Void>builder()
                .success(true)
                .code(200)
                .message("Từ chối gia hạn thành công")
                .build();
    }
    @GetMapping("/reports")
    public ApiResponse<List<ReportAdminResponse>> getReports() {
        return ApiResponse.<List<ReportAdminResponse>>builder()
                .success(true)
                .code(200)
                .message("Lấy danh sách báo cáo thành công")
                .data(adminService.getAllReportsForAdmin())
                .build();
    }
}
