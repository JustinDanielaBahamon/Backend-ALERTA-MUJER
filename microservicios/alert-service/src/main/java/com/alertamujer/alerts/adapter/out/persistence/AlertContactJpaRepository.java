package com.alertamujer.alerts.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

interface AlertContactJpaRepository extends JpaRepository<AlertContactJpaEntity, Long> {

    List<AlertContactJpaEntity> findByAlertId(Long alertId);

    @Query("select distinct ac.alertId from AlertContactJpaEntity ac where ac.channel = 'email' and lower(ac.destination) = lower(:correo)")
    List<Long> findAlertIdsNotifiedTo(@Param("correo") String correo);
}
