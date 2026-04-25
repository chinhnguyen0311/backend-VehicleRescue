package com.Doantotnghiep.vehicle_rescue.authentication.repository;

import com.Doantotnghiep.vehicle_rescue.authentication.entity.Account;
import com.Doantotnghiep.vehicle_rescue.authentication.enums.AccountStatus;
import com.Doantotnghiep.vehicle_rescue.system.dto.response.PendingAccountResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    boolean existsByPhoneNumber(String phoneNumber);

    Account findByRefreshTokenAndUsername(String token, String username);
    @Query("""
SELECT new com.Doantotnghiep.vehicle_rescue.system.dto.response.PendingAccountResponse(
    a.accountId,
    a.username,
    a.fullName,
    a.email,
    a.phoneNumber,
    m.type,
    m.workType,
    m.garageName,
    m.garageAddress,
    a.createdAt
)
FROM Account a
JOIN Mechanic m ON m.account = a
WHERE a.status = :status
""")
    List<PendingAccountResponse> getPendingAccounts(AccountStatus status);
}
