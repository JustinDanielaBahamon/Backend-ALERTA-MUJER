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

    /** Envíos registrados para una alerta (correo y push a cada contacto). */
    List<AlertRecipient> recipientsOf(Long alertId);

    /** Guarda el resultado de un envío: "sent" o "failed" (con el motivo). */
    void markDelivery(Long recipientId, String status, String errorMessage);

    /** Recorrido de la alerta, del primer punto al último. */
    List<LocationPoint> locationTrail(Long alertId);

    record ExpirationStatus(LocalDateTime lastCheck, int lastExpiredCount) {}
}
