package com.alertamujer.evidence.adapter.out.alerts;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.evidence.application.port.out.AlertsPort;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Output adapter to the alerts module, in memory (same process). When evidence is split into its own
 * microservice it is replaced by an HTTP client to alert-service; nothing else changes.
 */
@Component
class AlertsAdapter implements AlertsPort {

    private final AlertsApi alertsApi;

    AlertsAdapter(AlertsApi alertsApi) {
        this.alertsApi = alertsApi;
    }

    @Override
    public Optional<AlertSummary> find(Long alertId) {
        return alertsApi.findAlert(alertId);
    }

    @Override
    public Map<Long, AlertSummary> findMany(Collection<Long> alertIds) {
        return alertsApi.findAlerts(alertIds);
    }

    @Override
    public Map<Long, AlertSummary> all() {
        return alertsApi.alertsById();
    }

    @Override
    public List<Long> idsOfUser(Long userProfileId) {
        return alertsApi.alertIdsOfProfile(userProfileId);
    }

    @Override
    public Set<Long> keepEvidence(Collection<Long> alertIds) {
        return alertsApi.alertsKeepingEvidence(alertIds);
    }
}
