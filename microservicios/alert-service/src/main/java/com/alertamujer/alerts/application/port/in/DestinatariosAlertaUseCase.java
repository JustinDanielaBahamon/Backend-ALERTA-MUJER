package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.api.AlertRecipient;
import com.alertamujer.alerts.domain.model.Alert;

import java.time.LocalDateTime;
import java.util.List;

/** A quién se avisó en cada alerta y cómo va cada envío. */
public interface DestinatariosAlertaUseCase {

    /** Contactos avisados en la alerta, con el estado de cada canal. */
    List<ContactoNotificado> contactosDe(Alert alert);

    /** Envíos registrados de la alerta (para el módulo de notificaciones). */
    List<AlertRecipient> destinatarios(Long alertId);

    /** Resultado de un envío: "sent" o "failed" (con el motivo). */
    void registrarEntrega(Long destinatarioId, String status, String error);

    /** Alertas en las que se avisó a este correo. */
    List<Long> alertasAvisadasA(String email);

    record CanalEnvio(String channel, String destination, String status,
                      LocalDateTime sentAt, LocalDateTime deliveredAt) {}

    record ContactoNotificado(Long contactId, String name, String relationship, List<CanalEnvio> channels) {}
}
