package com.Doantotnghiep.vehicle_rescue.authentication.service.impl;

import com.Doantotnghiep.vehicle_rescue.authentication.dto.request.RegisterRequest;
import com.Doantotnghiep.vehicle_rescue.authentication.entity.Account;
import com.Doantotnghiep.vehicle_rescue.authentication.enums.AccountStatus;
import com.Doantotnghiep.vehicle_rescue.authentication.repository.AccountRepository;
import com.Doantotnghiep.vehicle_rescue.authentication.service.AccountService;
import com.Doantotnghiep.vehicle_rescue.common.exception.CustomException;
import com.Doantotnghiep.vehicle_rescue.common.exception.ErrorCode;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Mechanic;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicStatus;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicWorkType;
import com.Doantotnghiep.vehicle_rescue.rescue_management.repository.MechanicRepository;
import com.Doantotnghiep.vehicle_rescue.rescue_management.service.map.DistanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final MechanicRepository mechanicRepository;
    private final DistanceService distanceService;
    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);
    @Override
    public String registerAccount(RegisterRequest request) {
        log.info("Registering new account: {}", request.getUsername());

        // Kiểm tra username đã tồn tại
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // Kiểm tra email đã tồn tại
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Account account = Account.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .isActive(false)
                .emailVerified(false)
                .status(AccountStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastActive(LocalDateTime.now())
                .build();
        accountRepository.save(account);
        Mechanic.MechanicBuilder mechanicBuilder = Mechanic.builder()
                .account(account)
                .type(request.getType())
                .workType(request.getWorkType())
                .displayName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .status(MechanicStatus.OFFLINE)
                ;

        // Nếu là GARAGE thì set thêm thông tin garage
        if (request.getWorkType() == MechanicWorkType.GARAGE) {

            double[] coords = distanceService.getCoordinatesFromAddress(request.getGarageAddress());

            if (coords == null) {
                throw new CustomException(ErrorCode.INVALID_ADDRESS, "Không tìm được tọa độ từ địa chỉ");
            }

            Point garageLocation = GEOMETRY_FACTORY.createPoint(
                    new Coordinate(coords[1], coords[0]) // lng, lat
            );

            mechanicBuilder
                    .garageName(request.getGarageName())
                    .garageAddress(request.getGarageAddress())
                    .garageLocation(garageLocation);
        }
        mechanicRepository.save(mechanicBuilder.build());
        log.info("Account registered successfully: {}", request.getUsername());
        return "Đăng ký thành công! Vui lòng chờ admin duyệt.";
    }

    @Override
    public Optional<Account> getAccountById(UUID accountId) {
        return accountRepository.findById(accountId);
    }

    @Override
    public Optional<Account> getAccountByUsername(String username) {
        return accountRepository.findByUsername(username);
    }

    @Override
    public Optional<Account> getAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    @Override
    public Optional<Account> getAccountByUsernameOrEmail(String usernameOrEmail) {
        return accountRepository.findByUsernameOrEmail(usernameOrEmail);
    }

    @Override
    public Account updateAccount(Account account) {
        account.setUpdatedAt(LocalDateTime.now());
        return accountRepository.save(account);
    }

    @Override
    public void deleteAccount(UUID accountId) {
        accountRepository.deleteById(accountId);
    }

    @Override
    public boolean existsByUsername(String username) {
        return accountRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return accountRepository.existsByEmail(email);
    }

    @Override
    public void updateAccountToken(String token, String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        account.setRefreshToken(token);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
    }

    @Override
    public Account getAccountByRefreshTokenAndUsername(String token, String username) {
        return accountRepository.findByRefreshTokenAndUsername(token, username);
    }

    @Override
    public void changePassword(String username, String currentPassword, String newPassword) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, account.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        // Update password
        account.setPassword(passwordEncoder.encode(newPassword));
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);

        log.info("Password changed successfully for account: {}", username);
    }
}

