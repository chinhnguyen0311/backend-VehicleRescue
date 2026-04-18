package com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MechanicStatisticResponse {
    private int totalCompletedOrders;
    private BigDecimal averageRating;
    private double avgOrdersPerMonth;
    private double rankingScore;

    private List<RecentReviewItem> recentReviews;

    @Data
    @Builder
    public static class RecentReviewItem {
        private String customerName;
        private Integer rating;
        private String review;
        private String serviceName;
        private String createdAt;
    }
}
