package com.Doantotnghiep.vehicle_rescue.system.dto.response;

import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicWorkType;
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
public class SubscriptionResponse {
    private UUID subId;
    private String mechanicName;
    private MechanicWorkType workType;
    private OffsetDateTime currentEndDate;
    private OffsetDateTime newEndDate;
    private OffsetDateTime renewalDate;
    private String billImageUrl;
}
