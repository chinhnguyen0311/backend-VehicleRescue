package com.Doantotnghiep.vehicle_rescue.rescue_management.entity;

import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mechanic_subscriptions")
public class MechanicSubscription {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "mechanic_id")
    private Mechanic mechanic;

    private OffsetDateTime currentEndDate;
    private OffsetDateTime newEndDate;
    private OffsetDateTime renewalDate;

    private String billImageUrl;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;
}
