package com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRescueOrderRequest {
    private String customerName;
    private String customerPhone;
    private UUID serviceId;
    private Double latitude;
    private Double longitude;
    private UUID mechanicId;
}
