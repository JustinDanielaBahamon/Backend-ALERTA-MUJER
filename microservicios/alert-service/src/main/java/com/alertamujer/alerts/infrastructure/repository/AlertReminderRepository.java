package com.alertamujer.alerts.infrastructure.repository;

import com.alertamujer.alerts.domain.model.AlertReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertReminderRepository extends JpaRepository<AlertReminder, Long> {
    List<AlertReminder> findByAlertIdOrderBySequenceNumber(Long alertId);
    List<AlertReminder> findByStatusAndScheduledAtBefore(String status, LocalDateTime time);
}
