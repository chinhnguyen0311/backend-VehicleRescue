package com.Doantotnghiep.vehicle_rescue.rescue_management.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MechanicServiceId implements Serializable {
    private UUID mechanicId;
    private UUID serviceId;
}
