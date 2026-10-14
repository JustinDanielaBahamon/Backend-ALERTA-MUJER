package com.alertamujer.alerts.infrastructure.repository;

import com.alertamujer.alerts.domain.model.SavedRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedRouteRepository extends JpaRepository<SavedRoute, Long> {
    List<SavedRoute> findByUserProfileIdOrderByCreatedAtDesc(Long userProfileId);
    long countByUserProfileId(Long userProfileId);
    long countByUserProfileIdAndIsStarredTrue(Long userProfileId);
}
