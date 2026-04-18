package com.Doantotnghiep.vehicle_rescue.system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private long totalCompletedOrders;
    private long totalRequestedOrders;
    private long totalMechanics;
    private double avgOrdersPerMonth;

    private List<MonthlyStats> monthlyStats;
    private List<ServiceStats> serviceRanking;
    private List<MechanicStats> topMechanics;
}
