package com.alertamujer.resources.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface EmergencyResourceJpaRepository extends JpaRepository<EmergencyResourceJpaEntity, Long> {

    List<EmergencyResourceJpaEntity> findByCity(String city);

    List<EmergencyResourceJpaEntity> findByResourceType(String resourceType);
}
