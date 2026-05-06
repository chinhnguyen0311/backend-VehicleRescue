package com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MechanicOrderItemResponse {
    private UUID orderId;
    private String customerName;
    private String customerPhone;
    private String serviceName;
    private Double latitude;
    private Double longitude;
    private Double distance;
    private String address;
    private String status;
    private OffsetDateTime createdAt;
}
