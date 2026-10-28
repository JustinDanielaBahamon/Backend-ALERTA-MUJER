package com.alertamujer.evidence.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

interface EvidenceJpaRepository extends JpaRepository<EvidenceJpaEntity, Long> {

    List<EvidenceJpaEntity> findByAlertIdInOrderByCreatedAtDesc(Collection<Long> alertIds);

    List<EvidenceJpaEntity> findByAlertIdOrderByCreatedAtAsc(Long alertId);

    List<EvidenceJpaEntity> findByCreatedAtBefore(LocalDateTime before);
}
