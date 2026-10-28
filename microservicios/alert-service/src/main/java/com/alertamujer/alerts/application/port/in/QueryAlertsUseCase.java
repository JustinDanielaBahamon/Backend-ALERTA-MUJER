package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.Alert;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Alert queries asked by the other modules and microservices. */
public interface QueryAlertsUseCase {

    Optional<Alert> find(Long id);

    List<Alert> findMany(Collection<Long> ids);

    List<Alert> all();

    /** Number of alerts per user_profile.id. */
    Map<Long, Long> countByProfile();

    long activeCount();

    List<Long> profileIds(Long profile);

    /** Of those alerts, the ones the user marked to keep their evidence. */
    Set<Long> keepEvidence(Collection<Long> ids);
}
