package com.alertamujer.notifications.application.port.out;

import com.alertamujer.alerts.api.AlertMessageView;
import com.alertamujer.alerts.api.AlertRecipient;
import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.LocationPoint;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Puerto de salida hacia el módulo alerts: datos de la alerta, envíos, recorrido y chat. */
public interface AlertsPort {

    Optional<AlertSummary> find(Long alertId);

    List<AlertSummary> findMany(Collection<Long> alertIds);

    /** Envíos registrados de la alerta (correo y push a cada contacto). */
    List<AlertRecipient> recipients(Long alertId);

    /** "sent" o "failed" con el motivo. */
    void recordDelivery(Long recipientId, String status, String error);

    /** Alertas en las que se avisó a este correo como contacto de emergencia. */
    List<Long> notifiedTo(String email);

    List<LocationPoint> route(Long alertId);

    List<AlertMessageView> messages(Long alertId, Long afterId);

    AlertMessageView contactMessage(Long alertId, String name, String text);
}
