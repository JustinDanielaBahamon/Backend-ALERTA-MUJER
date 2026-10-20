package com.alertamujer.alerts.infrastructure.repository;

import com.alertamujer.alerts.domain.model.FrequentLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FrequentLocationRepository extends JpaRepository<FrequentLocation, Long> {
    List<FrequentLocation> findByUserProfileId(Long userProfileId);
    List<FrequentLocation> findByUserProfileIdAndIsActiveTrue(Long userProfileId);
}
