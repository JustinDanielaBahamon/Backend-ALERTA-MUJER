package com.alertamujer.evidence.application.port.out;

import com.alertamujer.evidence.domain.model.Evidence;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Output port: where evidence is stored (implemented by the persistence adapter). */
public interface EvidenceRepository {

    Evidence save(Evidence evidence);

    Optional<Evidence> find(Long id);

    List<Evidence> all();

    /** Evidence of those alerts, newest first. */
    List<Evidence> ofAlerts(Collection<Long> alertIds);

    /** Evidence of one alert, oldest first. */
    List<Evidence> ofAlert(Long alertId);

    List<Evidence> savedBefore(LocalDateTime date);

    void delete(Evidence evidence);

    void deleteAll(List<Evidence> evidences);
}
