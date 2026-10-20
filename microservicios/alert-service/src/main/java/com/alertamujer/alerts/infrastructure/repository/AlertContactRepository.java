package com.alertamujer.alerts.infrastructure.repository;

import com.alertamujer.alerts.domain.model.AlertContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertContactRepository extends JpaRepository<AlertContact, Long> {
    List<AlertContact> findByAlertId(Long alertId);

    /** Alertas en las que se avisó a este correo (el contacto que tiene cuenta en la app). */
    @Query("select distinct ac.alertId from AlertContact ac where ac.channel = 'email' and lower(ac.destination) = lower(:correo)")
    List<Long> findAlertIdsNotifiedTo(@Param("correo") String correo);
}
