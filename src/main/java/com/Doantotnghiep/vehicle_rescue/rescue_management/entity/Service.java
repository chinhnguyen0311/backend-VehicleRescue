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
@Table(name = "services")
public class Service {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "service_id", columnDefinition = "uuid")
    private UUID serviceId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "base_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal basePrice;
}
