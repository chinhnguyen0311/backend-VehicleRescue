package com.Doantotnghiep.vehicle_rescue.authentication.service;

import com.Doantotnghiep.vehicle_rescue.authentication.dto.LoginDTO;
import com.Doantotnghiep.vehicle_rescue.authentication.dto.request.ChangePasswordRequest;
import com.Doantotnghiep.vehicle_rescue.authentication.dto.request.RegisterRequest;
import com.Doantotnghiep.vehicle_rescue.authentication.dto.response.LoginResponseDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    /**
     * Đăng ký tài khoản mới
     */
    String register(RegisterRequest registerRequest);

    /**
     * Đăng nhập
     */
    LoginResponseDTO login(LoginDTO loginDto);

    /**
     * Lấy thông tin tài khoản hiện tại
     */
    LoginResponseDTO.AccountInfo getAccount();

    /**
     * Làm mới access token bằng refresh token
     */
    LoginResponseDTO refreshToken(String refreshToken);

    /**
     * Đăng xuất
     */
    void logout(HttpServletRequest request);

    /**
     * Đổi mật khẩu
     */
    void changePassword(ChangePasswordRequest request);
}

