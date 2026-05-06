package com.Doantotnghiep.vehicle_rescue.rescue_management.controller;

import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.AddMechanicServiceRequestDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.CreateReportRequest;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.UpdateMechanicServiceRequestDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request.UpdateProfileRequestDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.*;
import com.Doantotnghiep.vehicle_rescue.rescue_management.service.MechanicProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Dùng MockitoExtension thay cho WebMvcTest
@ExtendWith(MockitoExtension.class)
public class MechanicControllerTest {

    // Không dùng @Autowired nữa
    private MockMvc mockMvc;

    // Tự khởi tạo ObjectMapper
    private ObjectMapper objectMapper = new ObjectMapper();

    // Thay @MockBean bằng @Mock
    @Mock
    private MechanicProfileService mechanicService;

    // Inject các mock vào Controller
    @InjectMocks
    private MechanicController mechanicController;

    private UUID mockId;

    @BeforeEach
    void setUp() {
        mockId = UUID.randomUUID();
        // Tự build MockMvc độc lập
        mockMvc = MockMvcBuilders.standaloneSetup(mechanicController).build();
    }

    // =======================================================================
    // Test Profile Endpoints
    // =======================================================================

    @Test
    void getProfile_returnsProfile() throws Exception {
        ProfileResponseDTO profile = new ProfileResponseDTO();
        profile.setFullName("Nguyen Van Test");

        when(mechanicService.getProfile()).thenReturn(profile);

        mockMvc.perform(get("/mechanic/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lấy hồ sơ thành công"))
                .andExpect(jsonPath("$.data.fullName").value("Nguyen Van Test"));
    }

    @Test
    void updateProfile_success() throws Exception {
        ProfileResponseDTO responseDto = new ProfileResponseDTO();
        responseDto.setFullName("Updated Name");

        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy image content".getBytes()
        );

        when(mechanicService.updateProfile(any(UpdateProfileRequestDTO.class), any())).thenReturn(responseDto);

        mockMvc.perform(multipart(HttpMethod.PUT, "/mechanic/profile")
                        .file(file)
                        .param("fullName", "Updated Name")
                        // THÊM CÁC TRƯỜNG BẮT BUỘC KHÁC THEO DTO CỦA BẠN VÀO ĐÂY:
                        .param("phoneNumber", "0901234567")
                        .param("address", "123 Đường ABC")
                        .param("type", "MOTORBIKE")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cập nhật hồ sơ thành công"))
                .andExpect(jsonPath("$.data.fullName").value("Updated Name"));
    }

    // =======================================================================
    // Test Services Endpoints
    // =======================================================================

    @Test
    void addService_success() throws Exception {
        AddMechanicServiceRequestDTO request = new AddMechanicServiceRequestDTO();
        request.setServiceId(mockId);
        request.setCustomPrice(BigDecimal.valueOf(100.0));

        doNothing().when(mechanicService).addService(any(AddMechanicServiceRequestDTO.class));

        mockMvc.perform(post("/mechanic/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Thêm dịch vụ thành công"));
    }

    @Test
    void getServices_returnsList() throws Exception {
        MechanicServiceResponseDTO service = new MechanicServiceResponseDTO();
        service.setServiceName("Vá lốp");

        when(mechanicService.getServices()).thenReturn(Collections.singletonList(service));

        mockMvc.perform(get("/mechanic/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].serviceName").value("Vá lốp"));
    }

    @Test
    void updateServicePrice_success() throws Exception {
        UpdateMechanicServiceRequestDTO request = new UpdateMechanicServiceRequestDTO();
        request.setCustomPrice(BigDecimal.valueOf(150.0));

        doNothing().when(mechanicService).updateServicePrice(eq(mockId), any(UpdateMechanicServiceRequestDTO.class));

        mockMvc.perform(put("/mechanic/services/{serviceId}", mockId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cập nhật giá dịch vụ thành công"));
    }

    // =======================================================================
    // Test Order Status Endpoints
    // =======================================================================

    @Test
    void getRequestedOrders_returnsList() throws Exception {
        MechanicOrderItemResponse order = new MechanicOrderItemResponse();
        order.setCustomerName("Khach Hang Test");

        when(mechanicService.getRequestedOrdersForMechanic()).thenReturn(Collections.singletonList(order));

        mockMvc.perform(get("/mechanic/requested-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].customerName").value("Khach Hang Test"));
    }

    @Test
    void acceptOrder_success() throws Exception {
        doNothing().when(mechanicService).acceptOrder(mockId);

        mockMvc.perform(put("/mechanic/order/{orderId}/accept", mockId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Chấp nhận yêu cầu thành công"));
    }

    @Test
    void cancelOrder_success() throws Exception {
        doNothing().when(mechanicService).cancelOrder(mockId);

        mockMvc.perform(put("/mechanic/order/{orderId}/cancel", mockId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Từ chối yêu cầu thành công"));
    }

    @Test
    void inProcessOrder_success() throws Exception {
        doNothing().when(mechanicService).inProcessOrder(mockId);

        mockMvc.perform(put("/mechanic/order/{orderId}/in-process", mockId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Bắt đầu xử lý yêu cầu"));
    }

    @Test
    void completeOrder_success() throws Exception {
        doNothing().when(mechanicService).completeOrder(mockId);

        mockMvc.perform(put("/mechanic/order/{orderId}/complete", mockId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Hoàn thành yêu cầu thành công"));
    }

    // =======================================================================
    // Test Additional Endpoints
    // =======================================================================

    @Test
    void getCurrentOrder_returnsOrder() throws Exception {
        MechanicOrderItemResponse order = new MechanicOrderItemResponse();
        order.setCustomerName("Current Customer");

        when(mechanicService.getCurrentOrder()).thenReturn(order);

        mockMvc.perform(get("/mechanic/current-order"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.customerName").value("Current Customer"));
    }

    @Test
    void getOrderHistory_returnsHistoryList() throws Exception {
        MechanicOrderHistoryResponse history = new MechanicOrderHistoryResponse();
        when(mechanicService.getOrderHistory()).thenReturn(Collections.singletonList(history));

        mockMvc.perform(get("/mechanic/order-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lấy lịch sử đơn thành công"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getStatistic_returnsStatistic() throws Exception {
        MechanicStatisticResponse stat = new MechanicStatisticResponse();
        when(mechanicService.getStatistic()).thenReturn(stat);

        mockMvc.perform(get("/mechanic/statistic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lấy thống kê thành công"));
    }

    @Test
    void getMechanicDetail_returnsDetail() throws Exception {
        MechanicDetailResponse detail = new MechanicDetailResponse();
        // Hãy đảm bảo tên biến khớp với DTO của bạn (mechanicName hay displayName)
        detail.setMechanicName("Test Mechanic Name");

        when(mechanicService.getMechanicDetail(mockId)).thenReturn(detail);

        mockMvc.perform(get("/mechanic/detail/{mechanicId}", mockId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.mechanicName").value("Test Mechanic Name"));
    }

    @Test
    void subscribe_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "bill.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy content".getBytes()
        );

        doNothing().when(mechanicService).requestRenewal(any());

        mockMvc.perform(multipart("/mechanic/subscription")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Gửi yêu cầu gia hạn thành công"));
    }

    @Test
    void reportByMechanic_success() throws Exception {
        CreateReportRequest request = new CreateReportRequest();
        request.setOrderId(mockId);
        request.setContent("Khách hàng bom hàng");

        doNothing().when(mechanicService).reportByMechanic(any(CreateReportRequest.class));

        mockMvc.perform(post("/mechanic/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Báo cáo thành công"));
    }
}