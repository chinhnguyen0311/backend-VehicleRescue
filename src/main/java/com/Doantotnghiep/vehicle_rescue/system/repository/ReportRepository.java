package com.Doantotnghiep.vehicle_rescue.system.repository;

import com.Doantotnghiep.vehicle_rescue.system.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    @Query("""
SELECT r
FROM Report r
ORDER BY r.createdAt DESC
""")
    List<Report> findAllOrderByCreatedAtDesc();
}
