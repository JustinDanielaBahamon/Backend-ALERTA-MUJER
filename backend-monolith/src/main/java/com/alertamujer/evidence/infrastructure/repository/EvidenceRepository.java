package com.alertamujer.evidence.infrastructure.repository;

import com.alertamujer.evidence.domain.model.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvidenceRepository extends JpaRepository<Evidence, Long> {
}
