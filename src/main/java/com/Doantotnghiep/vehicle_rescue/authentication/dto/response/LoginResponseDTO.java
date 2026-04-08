package com.Doantotnghiep.vehicle_rescue.authentication.dto.response;

import com.Doantotnghiep.vehicle_rescue.authentication.enums.AccountRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn; // thời gian sống của token (seconds)

    // Thông tin account
    private AccountInfo account;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountInfo {
        private UUID accountId;
        private String username;
        private String email;
        private String fullName;
        private String avatarUrl;
        private String phoneNumber;
        private AccountRole role;
        private Boolean isActive;
        private Boolean emailVerified;
    }
}


