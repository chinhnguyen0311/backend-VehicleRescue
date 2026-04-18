package com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MechanicDetailResponse {
    private String mechanicName;
    private String mechanicPhone;
    private Double avgRating;
    private Long totalReviews;
    private String type;      // MOTORBIKE / CAR / TRUCK
    private String workType;  // GARAGE / MOBILE
    private String address;
}
