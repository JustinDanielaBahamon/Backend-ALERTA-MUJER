package com.alertamujer.notifications.infrastructure.repository;

import com.alertamujer.notifications.domain.model.TrackingLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrackingLinkRepository extends JpaRepository<TrackingLink, Long> {
    Optional<TrackingLink> findByTokenHash(String tokenHash);
}
