package com.alertamujer.alerts.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface AlertMessageJpaRepository extends JpaRepository<AlertMessageJpaEntity, Long> {

    List<AlertMessageJpaEntity> findByAlertIdAndIdGreaterThanOrderByIdAsc(Long alertId, Long afterId);

    long countByAlertId(Long alertId);
}
