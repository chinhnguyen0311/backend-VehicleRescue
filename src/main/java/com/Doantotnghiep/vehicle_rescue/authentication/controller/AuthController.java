package com.Doantotnghiep.vehicle_rescue.authentication.controller;

import com.Doantotnghiep.vehicle_rescue.authentication.dto.LoginDTO;
import com.Doantotnghiep.vehicle_rescue.authentication.dto.request.ChangePasswordRequest;
import com.Doantotnghiep.vehicle_rescue.authentication.dto.request.RegisterRequest;
import com.Doantotnghiep.vehicle_rescue.authentication.dto.response.LoginResponseDTO;
import com.Doantotnghiep.vehicle_rescue.authentication.service.AuthService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${vehicle-rescue.jwt.access-token-validity-in-seconds}")
    private long accessTokenExpiration;

    @Value("${vehicle-rescue.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> register(
            @ModelAttribute @Valid RegisterRequest request,
            @RequestPart(value = "file", required = false) MultipartFile fileImage) {
        String response = authService.register(request, fileImage);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTO loginDto) {
        LoginResponseDTO response = authService.login(loginDto);
        response.setExpiresIn(accessTokenExpiration);

        // Set cookies
        ResponseCookie resCookies = ResponseCookie
                .from("refresh_token", response.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(response);
    }

    @GetMapping("/account")
    public ResponseEntity<LoginResponseDTO.AccountInfo> getAccount() {
        LoginResponseDTO.AccountInfo accountInfo = authService.getAccount();
        return ResponseEntity.ok().body(accountInfo);
    }

    @GetMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> getRefreshToken(
            @CookieValue(name = "refresh_token", required = false) String cookieRefreshToken,
            @RequestHeader(name = "X-Refresh-Token", required = false) String headerRefreshToken) {

        // Priority: header (for mobile), fallback to cookie (for web)
        String refresh_token = headerRefreshToken != null ? headerRefreshToken : cookieRefreshToken;

        LoginResponseDTO response = authService.refreshToken(refresh_token);
        response.setExpiresIn(accessTokenExpiration);

        // Set cookies
        ResponseCookie resCookies = ResponseCookie
                .from("refresh_token", response.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        authService.logout(request);

        // Delete refresh token cookie
        ResponseCookie deleteCookie = ResponseCookie
                .from("refresh_token", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body("Đăng xuất thành công");
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }
}

