package com.Doantotnghiep.vehicle_rescue.interation.controller;

import com.Doantotnghiep.vehicle_rescue.common.dto.ApiResponse;
import com.Doantotnghiep.vehicle_rescue.interation.dto.request.CreateReviewRequest;
import com.Doantotnghiep.vehicle_rescue.interation.dto.response.TopMechanicResponse;
import com.Doantotnghiep.vehicle_rescue.interation.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    @PostMapping
    public ApiResponse<String> createReview(@RequestBody CreateReviewRequest request) {

        reviewService.createReview(request);

        return ApiResponse.<String>builder()
                .success(true)
                .code(200)
                .message("Đánh giá thành công")
                .data("OK")
                .build();
    }
    @GetMapping("/top-mechanics")
    public ApiResponse<List<TopMechanicResponse>> getTopMechanics() {
        return ApiResponse.<List<TopMechanicResponse>>builder()
                .success(true)
                .code(200)
                .message("Top 5 thợ sửa")
                .data(reviewService.getTop5Mechanics())
                .build();
    }
}
