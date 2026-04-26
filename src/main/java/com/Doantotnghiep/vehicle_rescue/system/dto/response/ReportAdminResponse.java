package com.Doantotnghiep.vehicle_rescue.system.dto.response;

import com.Doantotnghiep.vehicle_rescue.system.enums.ReportedByType;
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
public class ReportAdminResponse {
    private UUID reportId;

    private String reason;

    private ReportedByType reportedByType;

    private String reporterName;

    private String target;

    private String content;

    private OffsetDateTime createdAt;
}
