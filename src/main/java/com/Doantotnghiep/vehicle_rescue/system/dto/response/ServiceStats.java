package com.Doantotnghiep.vehicle_rescue.system.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceStats {
    private String serviceName;
    private long totalRequests;
}
