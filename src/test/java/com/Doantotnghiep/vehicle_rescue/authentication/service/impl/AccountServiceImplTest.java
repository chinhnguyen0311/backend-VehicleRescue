package com.Doantotnghiep.vehicle_rescue.authentication.service.impl;

import com.Doantotnghiep.vehicle_rescue.authentication.dto.request.RegisterRequest;
import com.Doantotnghiep.vehicle_rescue.authentication.entity.Account;
import com.Doantotnghiep.vehicle_rescue.authentication.enums.AccountStatus;
import com.Doantotnghiep.vehicle_rescue.authentication.repository.AccountRepository;
import com.Doantotnghiep.vehicle_rescue.common.exception.CustomException;
import com.Doantotnghiep.vehicle_rescue.common.exception.ErrorCode;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Mechanic;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicType;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicWorkType;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.MechanicRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.service.map.DistanceService;
import com.Doantotnghiep.vehicle_rescue.system.service.FirebaseStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MechanicRepository mechanicRepository;

    @Mock
    private DistanceService distanceService;

    @Mock
    private FirebaseStorageService storageService;

    @InjectMocks
    private AccountServiceImpl accountService;

    private RegisterRequest baseRegisterRequest;

    @BeforeEach
    void setUp() {
        baseRegisterRequest = new RegisterRequest();
        baseRegisterRequest.setUsername("testuser");
        baseRegisterRequest.setPassword("password123");
        baseRegisterRequest.setEmail("test@example.com");
        baseRegisterRequest.setFullName("Test User");
        baseRegisterRequest.setPhoneNumber("0123456789");
        baseRegisterRequest.setType(MechanicType.MOTORBIKE);
        baseRegisterRequest.setWorkType(MechanicWorkType.MOBILE);
    }

    // =======================================================================
    // Test registerAccount
    // =======================================================================

    @Test
    void registerAccount_mobile_successWithoutImage() {
        when(accountRepository.existsByUsername(anyString())).thenReturn(false);
        when(accountRepository.existsByEmail(anyString())).thenReturn(false);
        when(accountRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        String result = accountService.registerAccount(baseRegisterRequest, null);

        assertEquals("Đăng ký thành công! Vui lòng chờ admin duyệt.", result);
        verify(accountRepository).save(any(Account.class));
        verify(mechanicRepository).save(any(Mechanic.class));
        verify(distanceService, never()).getCoordinatesFromAddress(anyString()); // Không gọi distanceService vì là MOBILE
    }

    @Test
    void registerAccount_garage_successWithImage() throws IOException {
        baseRegisterRequest.setWorkType(MechanicWorkType.GARAGE);
        baseRegisterRequest.setGarageName("Garage Test");
        baseRegisterRequest.setGarageAddress("123 Street");

        MultipartFile mockFile = mock(MultipartFile.class);

        when(accountRepository.existsByUsername(anyString())).thenReturn(false);
        when(accountRepository.existsByEmail(anyString())).thenReturn(false);
        when(accountRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(storageService.uploadFile(mockFile)).thenReturn("http://image.url");
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        // [lat, lng]
        when(distanceService.getCoordinatesFromAddress("123 Street")).thenReturn(new double[]{21.0, 105.0});

        String result = accountService.registerAccount(baseRegisterRequest, mockFile);

        assertEquals("Đăng ký thành công! Vui lòng chờ admin duyệt.", result);
        verify(storageService).uploadFile(mockFile);
        verify(distanceService).getCoordinatesFromAddress("123 Street");
        verify(accountRepository).save(any(Account.class));
        verify(mechanicRepository).save(any(Mechanic.class));
    }

    @Test
    void registerAccount_garage_invalidAddress_throwsException() {
        baseRegisterRequest.setWorkType(MechanicWorkType.GARAGE);
        baseRegisterRequest.setGarageAddress("Invalid Street");

        when(accountRepository.existsByUsername(anyString())).thenReturn(false);
        when(accountRepository.existsByEmail(anyString())).thenReturn(false);
        when(accountRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(distanceService.getCoordinatesFromAddress(anyString())).thenReturn(null); // Không tìm thấy tọa độ

        CustomException exception = assertThrows(CustomException.class,
                () -> accountService.registerAccount(baseRegisterRequest, null));
        assertEquals(ErrorCode.INVALID_ADDRESS, exception.getErrorCode());
        verify(mechanicRepository, never()).save(any(Mechanic.class));
    }

    @Test
    void registerAccount_usernameExists_throwsException() {
        when(accountRepository.existsByUsername("testuser")).thenReturn(true);

        CustomException exception = assertThrows(CustomException.class,
                () -> accountService.registerAccount(baseRegisterRequest, null));
        assertEquals(ErrorCode.USERNAME_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    void registerAccount_emailExists_throwsException() {
        when(accountRepository.existsByUsername(anyString())).thenReturn(false);
        when(accountRepository.existsByEmail("test@example.com")).thenReturn(true);

        CustomException exception = assertThrows(CustomException.class,
                () -> accountService.registerAccount(baseRegisterRequest, null));
        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    void registerAccount_phoneExists_throwsException() {
        when(accountRepository.existsByUsername(anyString())).thenReturn(false);
        when(accountRepository.existsByEmail(anyString())).thenReturn(false);
        when(accountRepository.existsByPhoneNumber("0123456789")).thenReturn(true);

        CustomException exception = assertThrows(CustomException.class,
                () -> accountService.registerAccount(baseRegisterRequest, null));
        assertEquals(ErrorCode.PHONE_NUMBER_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    void registerAccount_uploadImageFails_throwsRuntimeException() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);

        when(accountRepository.existsByUsername(anyString())).thenReturn(false);
        when(accountRepository.existsByEmail(anyString())).thenReturn(false);
        when(accountRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(storageService.uploadFile(mockFile)).thenThrow(new IOException("S3 Error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> accountService.registerAccount(baseRegisterRequest, mockFile));
        assertTrue(exception.getMessage().contains("Lỗi upload ảnh"));
    }

    // =======================================================================
    // Test Simple Delegations & Updates
    // =======================================================================

    @Test
    void getAccountById_returnsAccount() {
        UUID id = UUID.randomUUID();
        Account account = new Account();
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));

        Optional<Account> result = accountService.getAccountById(id);
        assertTrue(result.isPresent());
        assertEquals(account, result.get());
    }

    @Test
    void updateAccount_updatesTimestampAndSaves() {
        Account account = new Account();
        when(accountRepository.save(account)).thenReturn(account);

        Account result = accountService.updateAccount(account);

        assertNotNull(result.getUpdatedAt());
        verify(accountRepository).save(account);
    }

    @Test
    void deleteAccount_callsRepository() {
        UUID id = UUID.randomUUID();
        accountService.deleteAccount(id);
        verify(accountRepository).deleteById(id);
    }

    // =======================================================================
    // Test updateAccountToken
    // =======================================================================

    @Test
    void updateAccountToken_success() {
        Account account = new Account();
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));

        accountService.updateAccountToken("new_token", "testuser");

        assertEquals("new_token", account.getRefreshToken());
        assertNotNull(account.getUpdatedAt());
        verify(accountRepository).save(account);
    }

    @Test
    void updateAccountToken_accountNotFound_throwsException() {
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> accountService.updateAccountToken("new_token", "testuser"));
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    // =======================================================================
    // Test changePassword
    // =======================================================================

    @Test
    void changePassword_success() {
        Account account = new Account();
        account.setPassword("encodedOldPassword");

        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        accountService.changePassword("testuser", "oldPassword", "newPassword");

        assertEquals("encodedNewPassword", account.getPassword());
        verify(accountRepository).save(account);
    }

    @Test
    void changePassword_invalidCurrentPassword_throwsException() {
        Account account = new Account();
        account.setPassword("encodedOldPassword");

        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrongOldPassword", "encodedOldPassword")).thenReturn(false);

        CustomException exception = assertThrows(CustomException.class,
                () -> accountService.changePassword("testuser", "wrongOldPassword", "newPassword"));
        assertEquals(ErrorCode.INVALID_PASSWORD, exception.getErrorCode());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void changePassword_accountNotFound_throwsException() {
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> accountService.changePassword("testuser", "oldPassword", "newPassword"));
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }
}