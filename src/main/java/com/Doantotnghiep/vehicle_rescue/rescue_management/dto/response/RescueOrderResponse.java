package com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response;

import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RescueOrderResponse {
    private UUID orderId;
    private String customerName;
    private String customerPhone;
    private Double latitude;
    private Double longitude;
    private String status;
    private UUID mechanicId;
    private UUID serviceId;
    private String mechanicName;
    private OrderStatus orderStatus;
}
