package com.Doantotnghiep.vehicle_rescue.rescue_management.entity;

import com.Doantotnghiep.vehicle_rescue.authentication.entity.Account;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicStatus;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.geo.Point;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mechanics")
public class Mechanic {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "mechanic_id", columnDefinition = "uuid")
    private UUID mechanicId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private MechanicType type;

    @Column(name = "display_name", length = 255, nullable = false)
    private String displayName;

    @Column(name = "phone_number", length = 20, nullable = false)
    private String phoneNumber;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "current_location", columnDefinition = "GEOMETRY(Point, 4326)")
    private Point currentLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, columnDefinition = "VARCHAR(50) DEFAULT 'OFFLINE'")
    private MechanicStatus status;

    @Column(name = "rating_score", precision = 3, scale = 2)
    private BigDecimal ratingScore;

    @Column(name = "total_reviews")
    private Integer totalReviews;

    @Column(name = "subs_end_date")
    private OffsetDateTime subsEndDate;

    @Column(name = "is_active_subs", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isActiveSubs;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = MechanicStatus.OFFLINE;
        }
        if (ratingScore == null) {
            ratingScore = BigDecimal.ZERO;
        }
        if (totalReviews == null) {
            totalReviews = 0;
        }
        if (isActiveSubs == null) {
            isActiveSubs = false;
        }
    }
}
