package com.alertamujer.alerts.infrastructure.repository;

import com.alertamujer.alerts.domain.model.AlertMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertMessageRepository extends JpaRepository<AlertMessage, Long> {
    /** Mensajes de la alerta posteriores a afterId (0 = todos), del más antiguo al más nuevo. */
    List<AlertMessage> findByAlertIdAndIdGreaterThanOrderByIdAsc(Long alertId, Long afterId);

    long countByAlertId(Long alertId);
}
