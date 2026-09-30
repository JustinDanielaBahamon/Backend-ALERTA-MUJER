package com.alertamujer.zones.infrastructure.repository;

import com.alertamujer.zones.domain.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    List<Zone> findByCity(String city);
    List<Zone> findByZoneType(String zoneType);
    List<Zone> findByIsActiveTrue();
}
