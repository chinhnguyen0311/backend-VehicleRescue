package com.Doantotnghiep.vehicle_rescue.rescue_management.dto.request;

import com.Doantotnghiep.vehicle_rescue.system.enums.ReasonCategory;
import com.Doantotnghiep.vehicle_rescue.system.enums.RelatedType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReportRequest {
    private UUID orderId;

    private String targetPhone;

    private RelatedType targetType;

    private ReasonCategory reasonCategory;

    private String content;
}
