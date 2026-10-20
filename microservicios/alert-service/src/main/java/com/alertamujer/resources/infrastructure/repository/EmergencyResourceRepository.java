package com.alertamujer.resources.infrastructure.repository;

import com.alertamujer.resources.domain.model.EmergencyResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyResourceRepository extends JpaRepository<EmergencyResource, Long> {
    List<EmergencyResource> findByCity(String city);
    List<EmergencyResource> findByResourceType(String resourceType);
    List<EmergencyResource> findByIsActiveTrue();
}
