package com.Doantotnghiep.vehicle_rescue.authentication.service.impl;

import com.Doantotnghiep.vehicle_rescue.authentication.dto.LoginDTO;
import com.Doantotnghiep.vehicle_rescue.authentication.dto.request.ChangePasswordRequest;
import com.Doantotnghiep.vehicle_rescue.authentication.dto.request.RegisterRequest;
import com.Doantotnghiep.vehicle_rescue.authentication.dto.response.LoginResponseDTO;
import com.Doantotnghiep.vehicle_rescue.authentication.entity.Account;
import com.Doantotnghiep.vehicle_rescue.authentication.service.AuthService;
import com.Doantotnghiep.vehicle_rescue.authentication.service.AccountService;
import com.Doantotnghiep.vehicle_rescue.authentication.util.SecurityUtil;
import com.Doantotnghiep.vehicle_rescue.common.exception.CustomException;
import com.Doantotnghiep.vehicle_rescue.common.exception.ErrorCode;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Mechanic;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.MechanicRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final AccountService accountService;
    private final MechanicRepository mechanicRepository;
    @Override
    public String register(RegisterRequest registerRequest, MultipartFile fileImage) {
        return accountService.registerAccount(registerRequest, fileImage);
    }

    @Override
    public LoginResponseDTO login(LoginDTO loginDto) {
        log.info("Account login attempt: {}", loginDto.getUsername());

        // Authenticate user
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDto.getUsername(), loginDto.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject()
                .authenticate(authenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get account from database
        Account account = accountService.getAccountByUsernameOrEmail(loginDto.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        // Check if account is active
        if (!account.getIsActive()) {
            LocalDateTime now = LocalDateTime.now();

            // Case 1: Temporary suspension
            if (account.getSuspendedUntil() != null) {
                if (now.isAfter(account.getSuspendedUntil())) {
                    // Suspension period ended → auto-unsuspend
                    log.info("Auto-unsuspending account {} - suspension period ended", account.getUsername());
                    account.setIsActive(true);
                    account.setSuspendedUntil(null);
                    accountService.updateAccount(account);
                } else {
                    // Still in suspension period
                    long daysRemaining = java.time.Duration.between(now, account.getSuspendedUntil()).toDays() + 1;
                    log.warn("Login attempt for suspended account: {} ({} days remaining)",
                            account.getUsername(), daysRemaining);
                    String message = String.format("Tài khoản của bạn đã bị tạm khóa. Còn %d ngày nữa sẽ tự động mở khóa.",
                            daysRemaining);
                    throw new CustomException(ErrorCode.ACCOUNT_NOT_ACTIVE, message);
                }
            }
            // Case 2: Permanent ban
            else if (account.getBannedAt() != null) {
                log.warn("Login attempt for permanently banned account: {} (banned at {})",
                        account.getUsername(), account.getBannedAt());
                throw new CustomException(ErrorCode.ACCOUNT_NOT_ACTIVE,
                        "Tài khoản của bạn đã bị cấm vĩnh viễn do vi phạm chính sách.");
            }
            // Case 3: Account not active for other reasons
            else {
                log.warn("Login attempt for inactive account: {}", account.getUsername());
                throw new CustomException(ErrorCode.ACCOUNT_NOT_ACTIVE);
            }
        }
        Optional<Mechanic> mechanicOpt = mechanicRepository.findByAccount(account);
        if (mechanicOpt.isPresent()) {
            Mechanic mechanic = mechanicOpt.get();
            if (mechanic.getSubsEndDate() != null && mechanic.getIsActiveSubs() != null && mechanic.getIsActiveSubs()) {
                LocalDateTime now = LocalDateTime.now();
                if (mechanic.getSubsEndDate() != null
                        && Boolean.TRUE.equals(mechanic.getIsActiveSubs())
                        && OffsetDateTime.now().isAfter(mechanic.getSubsEndDate())) {
                    mechanic.setIsActiveSubs(false);
                    mechanicRepository.save(mechanic);
                }
            }
        }

        // Update last active
        account.setLastActive(LocalDateTime.now());
        accountService.updateAccount(account);

        // Build AccountInfo
        LoginResponseDTO.AccountInfo accountInfo = buildAccountInfo(account);

        // Create tokens
        String accessToken = securityUtil.createAccessToken(authentication.getName(), accountInfo);
        String refreshToken = securityUtil.createRefreshToken(loginDto.getUsername(),
                LoginResponseDTO.builder().account(accountInfo).build());

        // Update refresh token in database
        accountService.updateAccountToken(refreshToken, loginDto.getUsername());

        log.info("Account logged in successfully: {}", account.getUsername());

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .account(accountInfo)
                .build();
    }

    @Override
    public LoginResponseDTO.AccountInfo getAccount() {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        Account account = accountService.getAccountByUsernameOrEmail(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        return buildAccountInfo(account);
    }

    @Override
    public LoginResponseDTO refreshToken(String refreshToken) {
        if (refreshToken == null) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        log.info("Refreshing token");

        // Validate refresh token
        Jwt decodedToken = securityUtil.checkValidRefreshToken(refreshToken);
        String username = decodedToken.getSubject();

        // Check account and token
        Account account = accountService.getAccountByRefreshTokenAndUsername(refreshToken, username);
        if (account == null) {
            log.warn("Invalid refresh token for account: {}", username);
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Build AccountInfo
        LoginResponseDTO.AccountInfo accountInfo = buildAccountInfo(account);

        // Create new tokens
        String newAccessToken = securityUtil.createAccessToken(username, accountInfo);
        String newRefreshToken = securityUtil.createRefreshToken(username,
                LoginResponseDTO.builder().account(accountInfo).build());

        // Update refresh token in database
        accountService.updateAccountToken(newRefreshToken, username);

        log.info("Token refreshed successfully for account: {}", username);

        return LoginResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .account(accountInfo)
                .build();
    }

    @Override
    public void logout(HttpServletRequest request) {
        log.info("Account logout attempt");

        // Get access token from header
        String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (accessToken == null || !accessToken.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        accessToken = accessToken.substring(7);

        // Validate access token
        Jwt decodedToken = securityUtil.checkValidAccessToken(accessToken);
        String username = decodedToken.getSubject();

        if (username == null || username.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        // Check if account exists
        Account account = accountService.getAccountByUsernameOrEmail(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        // Delete refresh token in database
        accountService.updateAccountToken(null, username);

        log.info("Account logged out successfully: {}", username);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        // Get username from JWT token
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ACCESS_TOKEN));

        // Check if new password and confirm password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }

        log.info("Changing password for account: {}", username);

        // Call service to change password
        accountService.changePassword(username, request.getCurrentPassword(), request.getNewPassword());

        log.info("Password changed successfully for account: {}", username);
    }

    /**
     * Helper method to build AccountInfo from Account entity
     */
    private LoginResponseDTO.AccountInfo buildAccountInfo(Account account) {
        return LoginResponseDTO.AccountInfo.builder()
                .accountId(account.getAccountId())
                .username(account.getUsername())
                .email(account.getEmail())
                .fullName(account.getFullName())
                .avatarUrl(account.getAvatarUrl())
                .phoneNumber(account.getPhoneNumber())
                .role(account.getRole())
                .isActive(account.getIsActive())
                .emailVerified(account.getEmailVerified())
                .build();
    }
}

