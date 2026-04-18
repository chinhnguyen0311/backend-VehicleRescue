package com.Doantotnghiep.vehicle_rescue.rescue_management.repository;

import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {
    @Query("""
SELECT s.name
FROM Service s
WHERE s.serviceId = :serviceId
""")
    String getServiceName(UUID serviceId);
}
