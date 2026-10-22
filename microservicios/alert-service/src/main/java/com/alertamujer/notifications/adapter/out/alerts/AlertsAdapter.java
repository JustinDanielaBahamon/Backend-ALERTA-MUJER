package com.alertamujer.notifications.adapter.out.alerts;

import com.alertamujer.alerts.api.AlertMessageView;
import com.alertamujer.alerts.api.AlertRecipient;
import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.alerts.api.LocationPoint;
import com.alertamujer.notifications.application.port.out.AlertsPort;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Adaptador de salida hacia el módulo alerts (mismo microservicio) por su contrato AlertsApi. */
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
    public List<AlertSummary> findMany(Collection<Long> alertIds) {
        return List.copyOf(alertsApi.findAlerts(alertIds).values());
    }

    @Override
    public List<AlertRecipient> recipients(Long alertId) {
        return alertsApi.recipientsOf(alertId);
    }

    @Override
    public void recordDelivery(Long recipientId, String status, String error) {
        alertsApi.markDelivery(recipientId, status, error);
    }

    @Override
    public List<Long> notifiedTo(String email) {
        return alertsApi.alertsNotifiedTo(email);
    }

    @Override
    public List<LocationPoint> route(Long alertId) {
        return alertsApi.locationTrail(alertId);
    }

    @Override
    public List<AlertMessageView> messages(Long alertId, Long afterId) {
        return alertsApi.messagesOf(alertId, afterId);
    }

    @Override
    public AlertMessageView contactMessage(Long alertId, String name, String text) {
        return alertsApi.addContactMessage(alertId, name, text);
    }
}
