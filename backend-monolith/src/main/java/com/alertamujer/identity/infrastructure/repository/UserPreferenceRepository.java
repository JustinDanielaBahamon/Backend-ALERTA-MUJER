package com.alertamujer.identity.infrastructure.repository;

import com.alertamujer.identity.domain.model.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    Optional<UserPreference> findByUserProfileId(Long userProfileId);
}
