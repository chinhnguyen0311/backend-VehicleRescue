package com.Doantotnghiep.vehicle_rescue.rescue_management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(MechanicServiceId.class)
@Table(name = "mechanic_services")
public class MechanicService {
    @Id
    @Column(name = "mechanic_id", columnDefinition = "uuid")
    private UUID mechanicId;

    @Id
    @Column(name = "service_id", columnDefinition = "uuid")
    private UUID serviceId;

    @Column(name = "custom_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal customPrice;
}
