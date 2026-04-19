package com.Doantotnghiep.vehicle_rescue.rescue_management.repository;

import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.MechanicSubscription;
import com.Doantotnghiep.vehicle_rescue.rescue_management.enums.SubscriptionStatus;
import com.Doantotnghiep.vehicle_rescue.system.dto.response.SubscriptionResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MechanicSubscriptionRepository extends JpaRepository<MechanicSubscription, UUID> {
    @Query("""
SELECT new com.Doantotnghiep.vehicle_rescue.system.dto.response.SubscriptionResponse(
    s.id,
    CASE 
        WHEN m.workType = com.Doantotnghiep.vehicle_rescue.rescue_management.enums.MechanicWorkType.GARAGE 
            THEN m.garageName
        ELSE m.displayName
    END,
    m.workType,
    s.currentEndDate,
    s.newEndDate,
    s.renewalDate,
    s.billImageUrl
)
FROM MechanicSubscription s
JOIN s.mechanic m
WHERE s.status = :status
ORDER BY s.renewalDate DESC
""")
    List<SubscriptionResponse> getAllSubscriptions(SubscriptionStatus status);

}
