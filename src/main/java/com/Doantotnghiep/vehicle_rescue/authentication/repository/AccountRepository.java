package com.Doantotnghiep.vehicle_rescue.authentication.repository;

import com.Doantotnghiep.vehicle_rescue.authentication.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByUsername(String username);

    Optional<Account> findByEmail(String email);

    @Query("SELECT a FROM Account a WHERE a.username = ?1 OR a.email = ?1")
    Optional<Account> findByUsernameOrEmail(String usernameOrEmail);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Account findByRefreshTokenAndUsername(String token, String username);
}
