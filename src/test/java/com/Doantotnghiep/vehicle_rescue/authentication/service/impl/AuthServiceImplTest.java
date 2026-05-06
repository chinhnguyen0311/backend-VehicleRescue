package com.Doantotnghiep.vehicle_rescue.authentication.service.impl;

import com.Doantotnghiep.vehicle_rescue.authentication.dto.LoginDTO;
import com.Doantotnghiep.vehicle_rescue.authentication.dto.request.ChangePasswordRequest;
import com.Doantotnghiep.vehicle_rescue.authentication.dto.request.RegisterRequest;
import com.Doantotnghiep.vehicle_rescue.authentication.dto.response.LoginResponseDTO;
import com.Doantotnghiep.vehicle_rescue.authentication.entity.Account;
import com.Doantotnghiep.vehicle_rescue.authentication.enums.AccountRole;
import com.Doantotnghiep.vehicle_rescue.authentication.service.AccountService;
import com.Doantotnghiep.vehicle_rescue.authentication.util.SecurityUtil;
import com.Doantotnghiep.vehicle_rescue.common.exception.CustomException;
import com.Doantotnghiep.vehicle_rescue.common.exception.ErrorCode;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Mechanic;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.MechanicRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private AccountService accountService;

    @Mock
    private MechanicRepository mechanicRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private MockedStatic<SecurityUtil> mockedSecurityUtil;

    @BeforeEach
    void setUp() {
        // Clear security context before each test
        SecurityContextHolder.clearContext();
        // Setup mock static cho các hàm gọi tĩnh (static method) của SecurityUtil
        mockedSecurityUtil = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtil.close();
        SecurityContextHolder.clearContext();
    }

    // =======================================================================
    // Test Register
    // =======================================================================
    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest();
        MultipartFile file = mock(MultipartFile.class);
        when(accountService.registerAccount(request, file)).thenReturn("Success");

        String result = authService.register(request, file);

        assertEquals("Success", result);
        verify(accountService, times(1)).registerAccount(request, file);
    }

    // =======================================================================
    // Test Login
    // =======================================================================
    @Test
    void login_success() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser");
        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        Account account = new Account();
        account.setUsername("testuser");
        account.setIsActive(true);
        when(accountService.getAccountByUsernameOrEmail("testuser")).thenReturn(Optional.of(account));

        when(mechanicRepository.findByAccount(account)).thenReturn(Optional.empty());
        when(securityUtil.createAccessToken(any(), any())).thenReturn("access_token");
        when(securityUtil.createRefreshToken(any(), any())).thenReturn("refresh_token");

        LoginResponseDTO response = authService.login(loginDTO);

        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertEquals("refresh_token", response.getRefreshToken());
        verify(accountService).updateAccount(account);
        verify(accountService).updateAccountToken("refresh_token", "testuser");
    }

    @Test
    void login_suspendedAccount_futureDate_throwsException() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        Account account = new Account();
        account.setUsername("testuser");
        account.setIsActive(false);
        // Bị khóa tới ngày mai
        account.setSuspendedUntil(LocalDateTime.now().plusDays(1));

        when(accountService.getAccountByUsernameOrEmail("testuser")).thenReturn(Optional.of(account));

        CustomException exception = assertThrows(CustomException.class, () -> authService.login(loginDTO));
        assertEquals(ErrorCode.ACCOUNT_NOT_ACTIVE, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Tài khoản của bạn đã bị tạm khóa"));
    }

    @Test
    void login_suspendedAccount_pastDate_autoUnsuspend() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser");
        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        Account account = new Account();
        account.setUsername("testuser");
        account.setIsActive(false);
        // Thời gian khóa đã qua
        account.setSuspendedUntil(LocalDateTime.now().minusDays(1));

        when(accountService.getAccountByUsernameOrEmail("testuser")).thenReturn(Optional.of(account));
        when(mechanicRepository.findByAccount(account)).thenReturn(Optional.empty());

        authService.login(loginDTO);

        assertTrue(account.getIsActive());
        assertNull(account.getSuspendedUntil());
        verify(accountService, atLeastOnce()).updateAccount(account);
    }

    @Test
    void login_bannedAccount_throwsException() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        Account account = new Account();
        account.setUsername("testuser");
        account.setIsActive(false);
        account.setBannedAt(LocalDateTime.now()); // Bị ban vĩnh viễn[cite: 3]

        when(accountService.getAccountByUsernameOrEmail("testuser")).thenReturn(Optional.of(account));

        CustomException exception = assertThrows(CustomException.class, () -> authService.login(loginDTO));
        assertEquals(ErrorCode.ACCOUNT_NOT_ACTIVE, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("bị cấm vĩnh viễn"));
    }

    @Test
    void login_mechanicSubsExpired_updatesSubsStatus() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("mechanic");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        Account account = new Account();
        account.setUsername("mechanic");
        account.setIsActive(true);
        when(accountService.getAccountByUsernameOrEmail("mechanic")).thenReturn(Optional.of(account));

        Mechanic mechanic = new Mechanic();
        mechanic.setIsActiveSubs(true);
        mechanic.setSubsEndDate(OffsetDateTime.now().minusDays(1)); // Đã hết hạn[cite: 3]
        when(mechanicRepository.findByAccount(account)).thenReturn(Optional.of(mechanic));

        authService.login(loginDTO);

        assertFalse(mechanic.getIsActiveSubs());
        verify(mechanicRepository).save(mechanic);
    }

    // =======================================================================
    // Test getAccount
    // =======================================================================
    @Test
    void getAccount_success() {
        mockedSecurityUtil.when(SecurityUtil::getCurrentUserLogin).thenReturn(Optional.of("testuser"));

        Account account = new Account();
        account.setUsername("testuser");
        account.setRole(AccountRole.MECHANIC); //[cite: 4]
        when(accountService.getAccountByUsernameOrEmail("testuser")).thenReturn(Optional.of(account));

        LoginResponseDTO.AccountInfo info = authService.getAccount();

        assertNotNull(info);
        assertEquals("testuser", info.getUsername());
    }

    // =======================================================================
    // Test refreshToken
    // =======================================================================
    @Test
    void refreshToken_success() {
        String token = "valid_refresh_token";
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("testuser");
        when(securityUtil.checkValidRefreshToken(token)).thenReturn(jwt);

        Account account = new Account();
        account.setUsername("testuser");
        when(accountService.getAccountByRefreshTokenAndUsername(token, "testuser")).thenReturn(account);

        when(securityUtil.createAccessToken(any(), any())).thenReturn("new_access");
        when(securityUtil.createRefreshToken(any(), any())).thenReturn("new_refresh");

        LoginResponseDTO response = authService.refreshToken(token);

        assertEquals("new_access", response.getAccessToken());
        assertEquals("new_refresh", response.getRefreshToken());
        verify(accountService).updateAccountToken("new_refresh", "testuser");
    }

    @Test
    void refreshToken_nullToken_throwsException() {
        CustomException exception = assertThrows(CustomException.class, () -> authService.refreshToken(null));
        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, exception.getErrorCode()); //[cite: 3]
    }

    // =======================================================================
    // Test logout
    // =======================================================================
    @Test
    void logout_success() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer valid_token");

        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("testuser");
        when(securityUtil.checkValidAccessToken("valid_token")).thenReturn(jwt);

        Account account = new Account();
        when(accountService.getAccountByUsernameOrEmail("testuser")).thenReturn(Optional.of(account));

        authService.logout(request);

        verify(accountService).updateAccountToken(null, "testuser"); //[cite: 3]
    }

    @Test
    void logout_invalidHeader_throwsException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("InvalidHeader");

        CustomException exception = assertThrows(CustomException.class, () -> authService.logout(request));
        assertEquals(ErrorCode.INVALID_ACCESS_TOKEN, exception.getErrorCode());
    }

    // =======================================================================
    // Test changePassword
    // =======================================================================
    @Test
    void changePassword_success() {
        mockedSecurityUtil.when(SecurityUtil::getCurrentUserLogin).thenReturn(Optional.of("testuser"));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPass");
        request.setNewPassword("newPass");
        request.setConfirmPassword("newPass");

        authService.changePassword(request);

        verify(accountService).changePassword("testuser", "oldPass", "newPass"); //[cite: 3]
    }

    @Test
    void changePassword_passwordMismatch_throwsException() {
        mockedSecurityUtil.when(SecurityUtil::getCurrentUserLogin).thenReturn(Optional.of("testuser"));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("newPass");
        request.setConfirmPassword("differentPass");

        CustomException exception = assertThrows(CustomException.class, () -> authService.changePassword(request));
        assertEquals(ErrorCode.PASSWORD_MISMATCH, exception.getErrorCode()); //[cite: 3]
    }
}