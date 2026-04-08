package com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMechanicServiceRequestDTO {
    @NotNull(message = "Giá không được để trống")
    private BigDecimal customPrice;
}
