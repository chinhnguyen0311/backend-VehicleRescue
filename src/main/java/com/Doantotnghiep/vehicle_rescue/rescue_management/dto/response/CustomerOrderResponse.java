package com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrderResponse {
    private String orderId;
    private String mechanicName;
    private String customerName;
    private String customerPhone;
    private String mechanicPhone;
    private String serviceName;
    private String status;
    private OffsetDateTime createdAt;
}
