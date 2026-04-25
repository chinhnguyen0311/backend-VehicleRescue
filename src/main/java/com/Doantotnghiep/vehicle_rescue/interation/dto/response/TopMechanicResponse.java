package com.Doantotnghiep.vehicle_rescue.interation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopMechanicResponse {
    private UUID mechanicId;
    private String mechanicName;
    private Double avgRating;
    private String avatarUrl;
    private Long totalReviews;
    private Long totalCompleted;
}
