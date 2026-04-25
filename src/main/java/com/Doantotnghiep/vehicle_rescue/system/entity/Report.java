package com.Doantotnghiep.vehicle_rescue.system.entity;

import com.Doantotnghiep.vehicle_rescue.system.enums.ReasonCategory;
import com.Doantotnghiep.vehicle_rescue.system.enums.RelatedType;
import com.Doantotnghiep.vehicle_rescue.system.enums.ReportStatus;
import com.Doantotnghiep.vehicle_rescue.system.enums.ReportedByType;
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
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "report_id", columnDefinition = "uuid")
    private UUID reportId;

    @Column(name = "order_id", columnDefinition = "uuid")
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reported_by_type", length = 50, nullable = false)
    private ReportedByType reportedByType;

    @Column(name = "reporter_phone", length = 20, nullable = false)
    private String reporterPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_category", length = 100, nullable = false)
    private ReasonCategory reasonCategory;

    @Column(name = "target_phone", length = 20, nullable = false)
    private String targetPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "reported_target", length = 50)
    private RelatedType reportedTarget;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, columnDefinition = "VARCHAR(50) DEFAULT 'PENDING'")
    private ReportStatus status;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (status == null) {
            status = ReportStatus.PENDING;
        }
    }
}
