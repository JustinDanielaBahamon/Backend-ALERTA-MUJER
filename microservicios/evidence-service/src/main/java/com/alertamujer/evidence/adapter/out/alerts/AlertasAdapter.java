package com.alertamujer.evidence.adapter.out.alerts;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.evidence.application.port.out.AlertasPort;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Adaptador de salida hacia el módulo alerts, en memoria (mismo proceso). Al separar evidence como
 * microservicio se reemplaza por un cliente HTTP hacia alert-service; el resto no cambia.
 */
@Component
class AlertasAdapter implements AlertasPort {

    private final AlertsApi alertsApi;

    AlertasAdapter(AlertsApi alertsApi) {
        this.alertsApi = alertsApi;
    }

    @Override
    public Optional<AlertSummary> buscar(Long alertId) {
        return alertsApi.findAlert(alertId);
    }

    @Override
    public Map<Long, AlertSummary> buscarVarias(Collection<Long> alertIds) {
        return alertsApi.findAlerts(alertIds);
    }

    @Override
    public Map<Long, AlertSummary> todas() {
        return alertsApi.alertsById();
    }

    @Override
    public List<Long> idsDeLaUsuaria(Long userProfileId) {
        return alertsApi.alertIdsOfProfile(userProfileId);
    }

    @Override
    public Set<Long> conservanEvidencias(Collection<Long> alertIds) {
        return alertsApi.alertsKeepingEvidence(alertIds);
    }
}
