package com.alertamujer.evidence.infrastructure.repository;

import com.alertamujer.evidence.domain.model.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvidenceRepository extends JpaRepository<Evidence, Long> {

    /** Evidencias de todas las alertas de una usuaria, de la más reciente a la más antigua. */
    @Query("select e from Evidence e where e.alertId in "
            + "(select a.id from Alert a where a.userProfileId = :userProfileId) order by e.createdAt desc")
    List<Evidence> findByOwnerProfileId(@Param("userProfileId") Long userProfileId);

    List<Evidence> findByAlertIdOrderByCreatedAtAsc(Long alertId);
}
