package com.Doantotnghiep.vehicle_rescue.interation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {
    private UUID orderId;
    private Integer rating;
    private String review;
    private String customerPhone;
}
