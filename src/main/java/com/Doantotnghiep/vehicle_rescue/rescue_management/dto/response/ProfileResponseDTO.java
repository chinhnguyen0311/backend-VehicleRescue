package com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response;

import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicType;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicWorkType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponseDTO {
    private String fullName;
    private String email;
    private String phoneNumber;
    private MechanicType type;
    private MechanicWorkType workType;
    private OffsetDateTime subsEndDate;
    // Chỉ có khi workType = GARAGE
    private String garageName;
    private String garageAddress;
    private String avatarUrl;
}
