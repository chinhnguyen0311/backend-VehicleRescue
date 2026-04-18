package com.Doantotnghiep.vehicle_rescue.system.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class MonthlyStats {
    private String month;
    private long completed;
    private long requested;
}
