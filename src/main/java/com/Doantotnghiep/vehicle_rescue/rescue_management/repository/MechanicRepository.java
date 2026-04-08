package com.Doantotnghiep.vehicle_rescue.rescue_management.repository;

import com.Doantotnghiep.vehicle_rescue.authentication.entity.Account;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Mechanic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface MechanicRepository extends JpaRepository<Mechanic, UUID> {
    boolean existsByAccount(Account account);
    Optional<Mechanic> findByAccount(Account account);
}
