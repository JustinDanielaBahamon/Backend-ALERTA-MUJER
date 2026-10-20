package com.alertamujer.notifications.application.port.out;

import com.alertamujer.alerts.api.AlertMessageView;
import com.alertamujer.alerts.api.AlertRecipient;
import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.LocationPoint;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Puerto de salida hacia el módulo alerts: datos de la alerta, envíos, recorrido y chat. */
public interface AlertasPort {

    Optional<AlertSummary> buscar(Long alertId);

    List<AlertSummary> buscarVarias(Collection<Long> alertIds);

    /** Envíos registrados de la alerta (correo y push a cada contacto). */
    List<AlertRecipient> destinatarios(Long alertId);

    /** "sent" o "failed" con el motivo. */
    void registrarEntrega(Long destinatarioId, String estado, String error);

    /** Alertas en las que se avisó a este correo como contacto de emergencia. */
    List<Long> avisadasA(String correo);

    List<LocationPoint> recorrido(Long alertId);

    List<AlertMessageView> mensajes(Long alertId, Long afterId);

    AlertMessageView mensajeDeContacto(Long alertId, String nombre, String texto);
}
