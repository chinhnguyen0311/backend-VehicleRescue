package com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MechanicOrderHistoryResponse {
    private UUID orderId;
    private String customerName;
    private String phone;
    private String serviceName;
    private String status;
    private String address;
    private OffsetDateTime completedAt;
    private Integer rating;
    private String review;
}
