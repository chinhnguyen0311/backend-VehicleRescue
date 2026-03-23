package com.Doantotnghiep.vehicle_rescue.rescue_management.repository;

import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.RescueOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface RescueOrderRepository extends JpaRepository<RescueOrder, UUID> {
}
