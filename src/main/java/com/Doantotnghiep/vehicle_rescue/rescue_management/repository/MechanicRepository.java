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
    List<Mechanic> findAll();
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
                       a.avatar_url, 
        ST_Distance(
            CASE 
                WHEN m.work_type = 'GARAGE' THEN m.garage_location
                ELSE m.current_location
            END::geography,
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
        ) / 1000 AS distance
    FROM mechanics m
    JOIN mechanic_services ms ON ms.mechanic_id = m.mechanic_id
    LeFT JOIN accounts a ON m.account_id = a.account_id
    WHERE ms.service_id = :serviceId
        AND m.is_active_subs = true
        AND m.type=:type
        AND m.status != 'BUSY'
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
            @Param("serviceId") UUID serviceId,
            @Param("type") String type);

    @Query(value = """
SELECT 
    m.mechanic_id,
    a.full_name,
    a.phone_number,
    a.email,
    COALESCE(AVG(r.rating), 0) AS avg_rating,
    COUNT(DISTINCT CASE WHEN o.status = 'COMPLETED' THEN o.order_id END) AS total_completed,
    a.created_at,
    m.subs_end_date,
    a.is_active,
    m.work_type,
    CASE WHEN m.work_type = 'GARAGE' THEN m.garage_name ELSE NULL END AS garage_name,
    CASE WHEN m.work_type = 'GARAGE' THEN m.garage_address ELSE NULL END AS garage_address,
    a.avatar_url
FROM mechanics m
JOIN accounts a ON m.account_id = a.account_id
LEFT JOIN rescue_orders o ON o.mechanic_id = m.mechanic_id
LEFT JOIN reviews r ON r.order_id = o.order_id
GROUP BY 
    m.mechanic_id,
    a.full_name,
    a.phone_number,
    a.email,
    a.created_at,
    m.subs_end_date,
    a.is_active,
    m.work_type,
    m.garage_name,
    m.garage_address,
    a.avatar_url
""", nativeQuery = true)
    List<Object[]> getAllMechanicStats();
}
