package com.Doantotnghiep.vehicle_rescue.authentication.service;

import com.Doantotnghiep.vehicle_rescue.authentication.dto.request.RegisterRequest;
import com.Doantotnghiep.vehicle_rescue.authentication.entity.Account;

import java.util.Optional;
import java.util.UUID;

public interface AccountService {
    String registerAccount(RegisterRequest request);

    Optional<Account> getAccountById(UUID accountId);

    Optional<Account> getAccountByUsername(String username);

    Optional<Account> getAccountByEmail(String email);

    Optional<Account> getAccountByUsernameOrEmail(String usernameOrEmail);

    Account updateAccount(Account account);

    void deleteAccount(UUID accountId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void updateAccountToken(String token, String username);

    Account getAccountByRefreshTokenAndUsername(String token, String username);

    void changePassword(String username, String currentPassword, String newPassword);
}

