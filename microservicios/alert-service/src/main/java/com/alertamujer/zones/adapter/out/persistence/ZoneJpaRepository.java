package com.alertamujer.zones.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface ZoneJpaRepository extends JpaRepository<ZoneJpaEntity, Long> {

    List<ZoneJpaEntity> findByCity(String city);

    List<ZoneJpaEntity> findByZoneType(String zoneType);
}
