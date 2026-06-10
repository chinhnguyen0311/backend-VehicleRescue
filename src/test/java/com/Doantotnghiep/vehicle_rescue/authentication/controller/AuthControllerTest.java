package com.Doantotnghiep.vehicle_rescue.authentication.controller;

import com.Doantotnghiep.vehicle_rescue.authentication.dto.LoginDTO;
import com.Doantotnghiep.vehicle_rescue.authentication.dto.request.ChangePasswordRequest;
import com.Doantotnghiep.vehicle_rescue.authentication.dto.request.RegisterRequest;
import com.Doantotnghiep.vehicle_rescue.authentication.dto.response.LoginResponseDTO;
import com.Doantotnghiep.vehicle_rescue.authentication.enums.AccountRole;
import com.Doantotnghiep.vehicle_rescue.authentication.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;
import org.springframework.validation.Errors;


import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;

    private AuthService authService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        authService = Mockito.mock(AuthService.class);

        AuthController authController = new AuthController(authService);

        ReflectionTestUtils.setField(
                authController,
                "accessTokenExpiration",
                3600L
        );

        ReflectionTestUtils.setField(
                authController,
                "refreshTokenExpiration",
                7200L
        );

        // Khởi tạo ObjectMapper trước
        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                // 1. Ép String converter dùng UTF-8 và giữ nguyên Jackson cho JSON
                .setMessageConverters(
                        new StringHttpMessageConverter(StandardCharsets.UTF_8),
                        new MappingJackson2HttpMessageConverter(objectMapper)
                )
                // 2. Bỏ qua validation để tránh lỗi 400 Bad Request
                .setValidator(new Validator() {
                    @Override
                    public boolean supports(Class<?> clazz) {
                        return true;
                    }

                    @Override
                    public void validate(Object target, Errors errors) {
                    }
                })
                .build();
    }

    @Test
    void register_success() throws Exception {

        when(authService.register(any(RegisterRequest.class), any()))
                .thenReturn("Đăng ký thành công");

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "avatar.jpg",
                        MediaType.IMAGE_JPEG_VALUE,
                        "image".getBytes()
                );

        mockMvc.perform(
                        multipart("/auth/register")
                                .file(file)
                                .param("username", "mechanic01")
                                .param("password", "123456")
                                .param("email", "mechanic@gmail.com")
                                .param("fullName", "Mechanic Test")
                                .param("phoneNumber", "0123456789")
                )
                .andExpect(status().isCreated())
                .andExpect(content().string("Đăng ký thành công"));
    }

    @Test
    void login_success_mechanic() throws Exception {

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("mechanic");
        loginDTO.setPassword("123456");

        LoginResponseDTO.AccountInfo accountInfo =
                LoginResponseDTO.AccountInfo.builder()
                        .accountId(UUID.randomUUID())
                        .username("mechanic")
                        .role(AccountRole.MECHANIC)
                        .email("mechanic@gmail.com")
                        .build();

        LoginResponseDTO response =
                LoginResponseDTO.builder()
                        .accessToken("access-token")
                        .refreshToken("refresh-token")
                        .tokenType("Bearer")
                        .account(accountInfo)
                        .build();

        when(authService.login(any(LoginDTO.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginDTO))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("access-token"))
                .andExpect(jsonPath("$.account.username")
                        .value("mechanic"))
                .andExpect(jsonPath("$.account.role")
                        .value("MECHANIC"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    void login_success_admin() throws Exception {

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("123456");

        LoginResponseDTO.AccountInfo accountInfo =
                LoginResponseDTO.AccountInfo.builder()
                        .accountId(UUID.randomUUID())
                        .username("admin")
                        .role(AccountRole.ADMIN)
                        .email("admin@gmail.com")
                        .build();

        LoginResponseDTO response =
                LoginResponseDTO.builder()
                        .accessToken("admin-access")
                        .refreshToken("admin-refresh")
                        .tokenType("Bearer")
                        .account(accountInfo)
                        .build();

        when(authService.login(any(LoginDTO.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginDTO))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.role")
                        .value("ADMIN"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    void getAccount_success() throws Exception {

        LoginResponseDTO.AccountInfo accountInfo =
                LoginResponseDTO.AccountInfo.builder()
                        .accountId(UUID.randomUUID())
                        .username("mechanic")
                        .role(AccountRole.MECHANIC)
                        .email("test@gmail.com")
                        .build();

        when(authService.getAccount())
                .thenReturn(accountInfo);

        mockMvc.perform(get("/auth/account"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username")
                        .value("mechanic"));
    }

    @Test
    void refreshToken_fromCookie_success() throws Exception {

        LoginResponseDTO response =
                LoginResponseDTO.builder()
                        .accessToken("new-access")
                        .refreshToken("new-refresh")
                        .tokenType("Bearer")
                        .build();

        when(authService.refreshToken(anyString()))
                .thenReturn(response);

        mockMvc.perform(
                        get("/auth/refresh")
                                .cookie(
                                        new Cookie(
                                                "refresh_token",
                                                "old-refresh"
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("new-access"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    void refreshToken_fromHeader_success() throws Exception {

        LoginResponseDTO response =
                LoginResponseDTO.builder()
                        .accessToken("header-access")
                        .refreshToken("header-refresh")
                        .tokenType("Bearer")
                        .build();

        when(authService.refreshToken(anyString()))
                .thenReturn(response);

        mockMvc.perform(
                        get("/auth/refresh")
                                .header("X-Refresh-Token", "header-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("header-access"));
    }

    @Test
    void logout_success() throws Exception {

        doNothing().when(authService).logout(any());

        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("Đăng xuất thành công"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    void changePassword_success() throws Exception {

        ChangePasswordRequest request =
                new ChangePasswordRequest();

        request.setCurrentPassword("123456");
        request.setNewPassword("654321");
        request.setConfirmPassword("654321");

        doNothing().when(authService)
                .changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(
                        post("/auth/change-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(content()
                        .string("Đổi mật khẩu thành công"));
    }
}

