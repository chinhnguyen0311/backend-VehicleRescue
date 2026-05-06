package com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MechanicDetailResponse {
    private String mechanicName;
    private String mechanicPhone;
    private String avatarUrl;
    private Double avgRating;
    private Long totalReviews;
    private String type;      // MOTORBIKE / CAR / TRUCK
    private String workType;  // GARAGE / MOBILE
    private String address;
    private List<ReviewSummary> recentReviews; // 👈 thêm

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewSummary {
        private String customerName;
        private Integer rating;
        private String review;
        private OffsetDateTime createdAt;
    }
}
