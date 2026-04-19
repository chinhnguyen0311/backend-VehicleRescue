package com.Doantotnghiep.vehicle_rescue.system.dto.response;

import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicType;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicWorkType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingAccountResponse {
    private UUID accountId;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private MechanicType type;
    private MechanicWorkType workType;
    private String garageName;
    private String garageAddress;
    private LocalDateTime createdAt;
}
