package com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationMessage {
    private Double latitude;
    private Double longitude;
}
