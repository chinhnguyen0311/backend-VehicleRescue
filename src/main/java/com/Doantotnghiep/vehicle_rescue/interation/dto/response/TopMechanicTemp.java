package com.Doantotnghiep.vehicle_rescue.interation.dto.response;

import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Mechanic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class TopMechanicTemp {
    Mechanic mechanic;
    double score;
    double avgRating;
    long totalReviews;
    long totalCompleted;
}
