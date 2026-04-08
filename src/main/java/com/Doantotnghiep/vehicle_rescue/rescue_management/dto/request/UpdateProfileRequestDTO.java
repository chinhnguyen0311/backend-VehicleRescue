package com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request;

import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequestDTO {
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;

    @NotNull(message = "Loại phương tiện không được để trống")
    private MechanicType type;

    // Chỉ cần khi workType = GARAGE
    private String garageName;
    private String garageAddress;
    private Double garageLatitude;
    private Double garageLongitude;
}
