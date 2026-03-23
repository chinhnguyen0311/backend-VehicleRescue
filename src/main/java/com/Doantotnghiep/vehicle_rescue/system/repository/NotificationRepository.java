package com.Doantotnghiep.vehicle_rescue.system.repository;

import com.Doantotnghiep.vehicle_rescue.system.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
}
