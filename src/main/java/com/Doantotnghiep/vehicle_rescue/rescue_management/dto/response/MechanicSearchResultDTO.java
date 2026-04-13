package com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MechanicSearchResultDTO {
    private UUID mechanicId;
    private String displayName;      // fullName nếu MOBILE, garageName nếu GARAGE
    private Double latitude;
    private Double longitude;
    private String phoneNumber;
    private List<String> type;       // Loại xe sửa được
    private List<MechanicServiceResponseDTO> services;
    private BigDecimal ratingScore;
    private Integer completedOrders; // Số đơn hoàn thành
    private Double distance;
}
