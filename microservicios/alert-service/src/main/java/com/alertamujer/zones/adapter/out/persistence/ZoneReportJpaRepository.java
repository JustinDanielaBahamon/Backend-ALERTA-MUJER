package com.alertamujer.zones.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface ZoneReportJpaRepository extends JpaRepository<ZoneReportJpaEntity, Long> {

    List<ZoneReportJpaEntity> findByZoneId(Long zoneId);

    List<ZoneReportJpaEntity> findByUserProfileId(Long userProfileId);

    List<ZoneReportJpaEntity> findByStatus(String status);
}
