package com.alertamujer.alerts.api;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Consultas del módulo alerts para los demás módulos. */
public interface AlertsApi {

    Optional<AlertSummary> findAlert(Long alertId);

    /** Alertas por id (las que no existen se omiten). */
    Map<Long, AlertSummary> findAlerts(Collection<Long> alertIds);

    /** Todas las alertas, por id. */
    Map<Long, AlertSummary> alertsById();

    /** Todas las alertas con su usuaria, de la más reciente a la más antigua (panel). */
    List<AdminAlertView> adminAlertViews();

    /** Número de alertas por user_profile.id. */
    Map<Long, Long> alertCountByProfile();

    long countActiveAlerts();

    /** Última revisión automática de alertas vencidas. */
    ExpirationStatus expirationStatus();

    /** Alertas en las que se avisó a este correo como contacto de emergencia. */
    List<Long> alertsNotifiedTo(String email);

    /** Envíos registrados para una alerta (correo y push a cada contacto). */
    List<AlertRecipient> recipientsOf(Long alertId);

    /** Guarda el resultado de un envío: "sent" o "failed" (con el motivo). */
    void markDelivery(Long recipientId, String status, String errorMessage);

    /** Chat de la alerta: mensajes posteriores a afterId (null = todos). */
    List<AlertMessageView> messagesOf(Long alertId, Long afterId);

    /** Mensaje de un contacto (desde el seguimiento). IllegalArgumentException si la alerta terminó. */
    AlertMessageView addContactMessage(Long alertId, String senderName, String body);

    /** Recorrido de la alerta, del primer punto al último. */
    List<LocationPoint> locationTrail(Long alertId);

    record ExpirationStatus(LocalDateTime lastCheck, int lastExpiredCount) {}
}
