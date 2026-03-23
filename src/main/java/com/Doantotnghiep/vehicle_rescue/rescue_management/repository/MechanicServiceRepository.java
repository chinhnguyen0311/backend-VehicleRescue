package com.Doantotnghiep.vehicle_rescue.rescue_management.repository;

import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.MechanicService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface MechanicServiceRepository extends JpaRepository<MechanicService, UUID> {
}
