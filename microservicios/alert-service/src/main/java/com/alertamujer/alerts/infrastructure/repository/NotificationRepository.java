package com.alertamujer.alerts.infrastructure.repository;

import com.alertamujer.alerts.domain.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserProfileId(Long userProfileId);
    List<Notification> findByIsReadFalse();
}
