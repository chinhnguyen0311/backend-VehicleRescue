package com.Doantotnghiep.vehicle_rescue.rescue_management.repository;

import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.RescueOrder;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface RescueOrderRepository extends JpaRepository<RescueOrder, UUID> {
    int countByMechanicIdAndStatus(UUID mechanicId, OrderStatus status);
    List<RescueOrder> findByMechanicIdAndStatus(UUID mechanicId, OrderStatus status);
    @Query("SELECT r FROM RescueOrder r WHERE r.mechanicId = :mechanicId AND r.status = 'REQUESTED'")
    List<RescueOrder> findRequestedByMechanicId(UUID mechanicId);
}
