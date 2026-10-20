package com.alertamujer.notifications.adapter.out.alerts;

import com.alertamujer.alerts.api.AlertMessageView;
import com.alertamujer.alerts.api.AlertRecipient;
import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.alerts.api.LocationPoint;
import com.alertamujer.notifications.application.port.out.AlertasPort;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Adaptador de salida hacia el módulo alerts (mismo microservicio) por su contrato AlertsApi. */
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
    public List<AlertSummary> buscarVarias(Collection<Long> alertIds) {
        return List.copyOf(alertsApi.findAlerts(alertIds).values());
    }

    @Override
    public List<AlertRecipient> destinatarios(Long alertId) {
        return alertsApi.recipientsOf(alertId);
    }

    @Override
    public void registrarEntrega(Long destinatarioId, String estado, String error) {
        alertsApi.markDelivery(destinatarioId, estado, error);
    }

    @Override
    public List<Long> avisadasA(String correo) {
        return alertsApi.alertsNotifiedTo(correo);
    }

    @Override
    public List<LocationPoint> recorrido(Long alertId) {
        return alertsApi.locationTrail(alertId);
    }

    @Override
    public List<AlertMessageView> mensajes(Long alertId, Long afterId) {
        return alertsApi.messagesOf(alertId, afterId);
    }

    @Override
    public AlertMessageView mensajeDeContacto(Long alertId, String nombre, String texto) {
        return alertsApi.addContactMessage(alertId, nombre, texto);
    }
}
