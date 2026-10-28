package com.alertamujer.notifications.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface TrackingLinkJpaRepository extends JpaRepository<TrackingLinkJpaEntity, Long> {

    Optional<TrackingLinkJpaEntity> findByTokenHash(String tokenHash);
}
