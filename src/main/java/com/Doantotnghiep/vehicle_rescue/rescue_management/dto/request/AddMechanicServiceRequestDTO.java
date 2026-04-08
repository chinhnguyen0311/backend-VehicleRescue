package com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddMechanicServiceRequestDTO {
    @NotNull(message = "Service không được để trống")
    private UUID serviceId;

    @NotNull(message = "Giá không được để trống")
    private BigDecimal customPrice;
}
