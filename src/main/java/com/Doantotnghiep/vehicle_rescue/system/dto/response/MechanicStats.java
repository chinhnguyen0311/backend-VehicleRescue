package com.Doantotnghiep.vehicle_rescue.system.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MechanicStats {
    private String mechanicName;
    private double avgRating;
    private long totalCompleted;
}
