package com.Doantotnghiep.vehicle_rescue.system.dto.response;

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
public class MechanicAdminResponse {
    private UUID mechanicId;
    private String mechanicName;
    private String mechanicPhone;
    private String mechanicEmail;
    private String avatarUrl;
    private Double avgRating;
    private Long totalCompleted;

    private String workType;
    private String garageName;
    private String garageAddress;

    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private Boolean isActive;
}
