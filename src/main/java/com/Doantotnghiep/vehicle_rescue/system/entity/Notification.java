package com.Doantotnghiep.vehicle_rescue.system.entity;

import com.Doantotnghiep.vehicle_rescue.authentication.entity.Account;
import com.Doantotnghiep.vehicle_rescue.system.enums.NotificationType;
import com.Doantotnghiep.vehicle_rescue.system.enums.RelatedType;
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
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "notification_id", columnDefinition = "uuid")
    private UUID notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private NotificationType type;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "related_id", columnDefinition = "uuid")
    private UUID relatedId;

    @Enumerated(EnumType.STRING)
    @Column(name = "related_type", length = 50)
    private RelatedType relatedType;

    @Column(name = "is_read", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isRead;

    @Column(name = "is_sent", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isSent;

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (isRead == null) {
            isRead = false;
        }
        if (isSent == null) {
            isSent = false;
        }
    }
}
