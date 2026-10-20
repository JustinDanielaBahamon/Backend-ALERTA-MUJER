package com.alertamujer.alerts.infrastructure.repository;

import com.alertamujer.alerts.domain.model.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {
    List<EmergencyContact> findByUserProfileId(Long userProfileId);
}
