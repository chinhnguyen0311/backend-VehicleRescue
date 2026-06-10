package com.Doantotnghiep.vehicle_rescue.interation.controller;

import com.Doantotnghiep.vehicle_rescue.interation.dto.request.CreateReviewRequest;
import com.Doantotnghiep.vehicle_rescue.interation.dto.response.TopMechanicResponse;
import com.Doantotnghiep.vehicle_rescue.interation.service.ReviewService;
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
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewControllerTest {

    private MockMvc mockMvc;
    private ReviewService reviewService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reviewService = Mockito.mock(ReviewService.class);
        ReviewController reviewController = new ReviewController(reviewService);

        // 1. Khởi tạo và cấu hình ObjectMapper hỗ trợ Java 8 LocalDateTime
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // (Tùy chọn) Tắt tính năng ghi ngày tháng dưới dạng Timestamp để xuất ra chuẩn ISO-8601 dễ đọc hơn
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 2. Cấu hình MockMvc
        mockMvc = MockMvcBuilders
                .standaloneSetup(reviewController)
                .setMessageConverters(
                        new StringHttpMessageConverter(StandardCharsets.UTF_8),
                        new MappingJackson2HttpMessageConverter(objectMapper)
                )
                .build();
    }

    @Test
    void createReview_success() throws Exception {
        // 1. Chuẩn bị dữ liệu đầu vào
        CreateReviewRequest request = new CreateReviewRequest();
        // Gán các giá trị cần thiết cho request nếu dto của bạn có (VD: request.setRating(5)...)

        // 2. Giả lập hành vi của ReviewService
        doNothing().when(reviewService).createReview(any(CreateReviewRequest.class));

        // 3. Thực thi API và kiểm tra kết quả
        mockMvc.perform(
                        post("/reviews")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Đánh giá thành công"))
                .andExpect(jsonPath("$.data").value("OK"));
    }

    @Test
    void getTopMechanics_success() throws Exception {
        // 1. Chuẩn bị dữ liệu giả lập (mock data)
        TopMechanicResponse mechanic1 = new TopMechanicResponse();
        // Có thể set thêm giá trị cho mechanic1 ở đây nếu cần thiết

        TopMechanicResponse mechanic2 = new TopMechanicResponse();

        List<TopMechanicResponse> mockTopMechanics = Arrays.asList(mechanic1, mechanic2);

        // 2. Giả lập hành vi của ReviewService
        when(reviewService.getTop5Mechanics()).thenReturn(mockTopMechanics);

        // 3. Thực thi API và kiểm tra kết quả
        mockMvc.perform(
                        get("/reviews/top-mechanics")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Top 5 thợ sửa"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }
}