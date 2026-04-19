package com.Doantotnghiep.vehicle_rescue.authentication.entity;

import com.Doantotnghiep.vehicle_rescue.authentication.enums.AccountRole;
import com.Doantotnghiep.vehicle_rescue.authentication.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "username", length = 100, nullable = false, unique = true)
    private String username;

    @Column(name = "email", length = 255, unique = true)
    private String email;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String password;

    @Column(name = "avatar_url", length = 1000)
    private String avatarUrl;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "role", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AccountRole role = AccountRole.MECHANIC;

    @Column(name = "google_id", unique = true, length = 100)
    private String googleId;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;
    @Enumerated(EnumType.STRING)
    private AccountStatus status;
    @Column(name = "banned_at")
    private LocalDateTime bannedAt;

    @Column(name = "last_active")
    private LocalDateTime lastActive;

    @Column(name = "refresh_token", length = 1000)
    private String refreshToken;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "suspended_until")
    private LocalDateTime suspendedUntil;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (role == null) {
            role = AccountRole.MECHANIC;
        }
        if (isActive == null) {
            isActive = true;
        }
        if (emailVerified == null) {
            emailVerified = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
