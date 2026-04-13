package com.Doantotnghiep.vehicle_rescue.rescue_management.repository;

import com.Doantotnghiep.vehicle_rescue.authentication.entity.Account;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Mechanic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface MechanicRepository extends JpaRepository<Mechanic, UUID> {
    boolean existsByAccount(Account account);
    Optional<Mechanic> findByAccount(Account account);
    @Query(value = """
    SELECT m.mechanic_id,
                       m.type,
                       m.display_name,
                       m.phone_number,
                       m.current_location,
                       m.garage_name,
                       m.garage_location,
                       m.work_type,
                       m.rating_score, 
        ST_Distance(
            CASE 
                WHEN m.work_type = 'GARAGE' THEN m.garage_location
                ELSE m.current_location
            END::geography,
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
        ) / 1000 AS distance
    FROM mechanics m
    JOIN mechanic_services ms ON ms.mechanic_id = m.mechanic_id
    WHERE ms.service_id = :serviceId
        AND m.is_active_subs = true
        AND (
            CASE 
                WHEN m.work_type = 'GARAGE' THEN m.garage_location
                ELSE m.current_location
            END
        ) IS NOT NULL
    ORDER BY distance ASC
    LIMIT 20
    """, nativeQuery = true)
    List<Object[]> findNearbyMechanics(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("serviceId") UUID serviceId);

}
