package com.Doantotnghiep.vehicle_rescue.interation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class MechanicRankingDTO {
    private UUID mechanicId;
    private double score;
}
