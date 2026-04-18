package com.Doantotnghiep.vehicle_rescue.rescue_management.repository;

import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.RescueOrder;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface RescueOrderRepository extends JpaRepository<RescueOrder, UUID> {
    int countByMechanicIdAndStatus(UUID mechanicId, OrderStatus status);
    List<RescueOrder> findByMechanicIdAndStatus(UUID mechanicId, OrderStatus status);
    boolean existsByMechanicIdAndStatusIn(UUID mechanicId, List<OrderStatus> statuses);
    Optional<RescueOrder> findFirstByMechanicIdAndStatusIn(UUID mechanicId, List<OrderStatus> statuses);
    @Query("SELECT r FROM RescueOrder r WHERE r.mechanicId = :mechanicId AND r.status = 'REQUESTED'")
    List<RescueOrder> findRequestedByMechanicId(UUID mechanicId);
    long countByStatus(OrderStatus status);
    @Query("""
SELECT o FROM RescueOrder o
WHERE o.mechanicId = :mechanicId
AND o.status = 'COMPLETED'
ORDER BY o.createdAt DESC
""")
    List<RescueOrder> findCompletedOrdersByMechanic(UUID mechanicId);
    @Query("""
SELECT r FROM RescueOrder r
WHERE r.customerPhone = :phone
AND r.status NOT IN ('CANCELLED')
AND r.createdAt >= :startOfDay
AND r.createdAt < :endOfDay
ORDER BY r.createdAt DESC
""")
    List<RescueOrder> findActiveByPhoneToday(
            @Param("phone") String phone,
            @Param("startOfDay") OffsetDateTime startOfDay,
            @Param("endOfDay") OffsetDateTime endOfDay
    );
    @Query("""
SELECT COUNT(r)
FROM RescueOrder r
WHERE r.mechanicId = :mechanicId
AND r.status = 'COMPLETED'
""")
    int countCompleted(UUID mechanicId);

    @Query("""
SELECT MIN(r.createdAt)
FROM RescueOrder r
WHERE r.mechanicId = :mechanicId
""")
    OffsetDateTime getFirstOrderDate(UUID mechanicId);

    @Query("""
SELECT r.mechanicId, COUNT(r.orderId)
FROM RescueOrder r
WHERE r.status = 'COMPLETED'
GROUP BY r.mechanicId
""")
    List<Object[]> countCompletedOrders();
}
