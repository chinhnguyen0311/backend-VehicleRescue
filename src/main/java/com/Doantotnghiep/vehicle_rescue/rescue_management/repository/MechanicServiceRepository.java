package com.Doantotnghiep.vehicle_rescue.rescue_management.repository;

import com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.MechanicServiceResponseDTO;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.MechanicService;
import com.Doantotnghiep.vehicle_rescue.rescue_management.entity.MechanicServiceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface MechanicServiceRepository extends JpaRepository<MechanicService, MechanicServiceId> {
    List<MechanicService> findByMechanicId(UUID mechanicId);
    boolean existsByMechanicIdAndServiceId(UUID mechanicId, UUID serviceId);

    @Query("""
        SELECT new com.Doantotnghiep.vehicle_rescue.rescue_management.dto.response.MechanicServiceResponseDTO(
            ms.serviceId,
            s.name,
            ms.customPrice
        )
        FROM MechanicService ms
        JOIN Service s ON ms.serviceId = s.serviceId
        WHERE ms.mechanicId = :mechanicId
    """)
    List<MechanicServiceResponseDTO> findServicesByMechanicId(@Param("mechanicId") UUID mechanicId);
}
