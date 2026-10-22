package com.alertamujer.evidence.application.port.out;

import com.alertamujer.alerts.api.AlertSummary;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Output port: what evidence needs to know about alerts. Today the alerts module answers it in memory
 * (same alert-service microservice).
 */
public interface AlertsPort {

    Optional<AlertSummary> find(Long alertId);

    Map<Long, AlertSummary> findMany(Collection<Long> alertIds);

    Map<Long, AlertSummary> todas();

    /** Alert ids of a user (user_profile.id). */
    java.util.List<Long> idsOfUser(Long userProfileId);

    /** Of those alerts, the ones the user marked to keep their evidence. */
    Set<Long> keepEvidence(Collection<Long> alertIds);
}
