package com.Doantotnghiep.vehicle_rescue.system.controller;

import com.Doantotnghiep.vehicle_rescue.common.dto.PageResponse;
import com.Doantotnghiep.vehicle_rescue.system.dto.response.*;
import com.Doantotnghiep.vehicle_rescue.system.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminControllerTest {

    private MockMvc mockMvc;
    private AdminService adminService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        adminService = Mockito.mock(AdminService.class);
        AdminController adminController = new AdminController(adminService);

        // Khởi tạo ObjectMapper với JavaTimeModule để tránh lỗi LocalDateTime
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders
                .standaloneSetup(adminController)
                // Ép Converter sử dụng UTF-8 để không bị lỗi font chữ tiếng Việt
                .setMessageConverters(
                        new StringHttpMessageConverter(StandardCharsets.UTF_8),
                        new MappingJackson2HttpMessageConverter(objectMapper)
                )
                .build();
    }

    @Test
    void getDashBoard_success() throws Exception {
        AdminDashboardResponse mockResponse = new AdminDashboardResponse();
        when(adminService.getDashboard()).thenReturn(mockResponse);

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Lấy thống kê thành công"));
    }

    @Test
    void getMechanics_success() throws Exception {
        List<MechanicAdminResponse> mockList = Collections.singletonList(new MechanicAdminResponse());
        when(adminService.getAllMechanicsForAdmin()).thenReturn(mockList);

        mockMvc.perform(get("/admin/mechanics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Lấy danh sách thợ máy thành công"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getPendingAccounts_success() throws Exception {
        List<PendingAccountResponse> mockList = Collections.singletonList(new PendingAccountResponse());
        when(adminService.getPendingAccounts()).thenReturn(mockList);

        mockMvc.perform(get("/admin/pending-accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Lấy danh sách tài khoản đang chờ duyệt thành công"));
    }

    @Test
    void approveAccount_success() throws Exception {
        UUID accountId = UUID.randomUUID();
        doNothing().when(adminService).approveAccount(any(UUID.class));

        mockMvc.perform(put("/admin/pending-accounts/{accountId}/approve", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Duyệt tài khoản thành công"));
    }

    @Test
    void rejectAccount_success() throws Exception {
        UUID accountId = UUID.randomUUID();
        doNothing().when(adminService).rejectAccount(any(UUID.class));

        mockMvc.perform(put("/admin/pending-accounts/{accountId}/reject", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Từ chối tài khoản thành công"));
    }

    @Test
    void getSubscriptions_success() throws Exception {
        List<SubscriptionResponse> mockList = Collections.singletonList(new SubscriptionResponse());
        when(adminService.getPendingSubscriptions()).thenReturn(mockList);

        mockMvc.perform(get("/admin/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Lấy danh sách gia hạn đang chờ duyệt thành công"));
    }

    @Test
    void approveSubscription_success() throws Exception {
        UUID subscriptionId = UUID.randomUUID();
        doNothing().when(adminService).approveSubscription(any(UUID.class));

        mockMvc.perform(put("/admin/subscriptions/{subscriptionId}/approve", subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Duyệt gia hạn thành công"));
    }

    @Test
    void rejectSubscription_success() throws Exception {
        UUID subscriptionId = UUID.randomUUID();
        doNothing().when(adminService).rejectSubscription(any(UUID.class));

        mockMvc.perform(put("/admin/subscriptions/{subscriptionId}/reject", subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Từ chối gia hạn thành công"));
    }

    @Test
    void getReports_success() throws Exception {
        List<ReportAdminResponse> mockList = Collections.singletonList(new ReportAdminResponse());
        when(adminService.getAllReportsForAdmin()).thenReturn(mockList);

        mockMvc.perform(get("/admin/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Lấy danh sách báo cáo thành công"));
    }

    @Test
    void getOrdersHistory_success() throws Exception {
        PageResponse<OrderHistoryResponse> mockPage = new PageResponse<>();
        when(adminService.getAllOrdersForAdmin(anyInt(), anyInt())).thenReturn(mockPage);

        mockMvc.perform(get("/admin/orders-history")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Lấy lịch sử đơn hàng thành công"));
    }

    @Test
    void banAccount_success() throws Exception {
        UUID mechanicId = UUID.randomUUID();
        doNothing().when(adminService).banAccount(any(UUID.class));

        mockMvc.perform(put("/admin/mechanics/{mechanicId}/ban", mechanicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Cấm vĩnh viễn tài khoản thành công"));
    }
}