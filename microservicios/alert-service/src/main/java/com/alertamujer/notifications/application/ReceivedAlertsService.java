package com.alertamujer.notifications.application;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Alertas que recibió la usuaria como contacto de emergencia de otra persona (se le avisó a su
 * correo). Desde el panel web puede ver cuáles están activas y abrir su seguimiento.
 */
@Service
public class ReceivedAlertsService {

    static final Duration RECIENTES = Duration.ofDays(30);

    private final AlertsApi alertsApi;
    private final IdentityApi identityApi;
    private final TrackingLinkService trackingLinkService;

    public ReceivedAlertsService(AlertsApi alertsApi, IdentityApi identityApi, TrackingLinkService trackingLinkService) {
        this.alertsApi = alertsApi;
        this.identityApi = identityApi;
        this.trackingLinkService = trackingLinkService;
    }

    /** Alertas recibidas en los últimos 30 días, de la más reciente a la más antigua. */
    @Transactional(readOnly = true)
    public List<ReceivedAlert> recibidas(Long userId, LocalDateTime ahora) {
        String correo = identityApi.findUser(userId).map(UserSummary::email).orElse(null);
        Long miPerfil = identityApi.findProfileId(userId).orElse(null);
        return alertsApi.findAlerts(alertsApi.alertsNotifiedTo(correo)).values().stream()
                .filter(a -> !Objects.equals(a.userProfileId(), miPerfil)) // las propias no son "recibidas"
                .filter(a -> a.startedAt() != null && a.startedAt().isAfter(ahora.minus(RECIENTES)))
                .sorted(Comparator.comparing(AlertSummary::startedAt).reversed())
                .map(a -> aVista(a, ahora))
                .toList();
    }

    /**
     * Crea un enlace de seguimiento para que la usuaria vea la alerta (la misma página que abre el
     * contacto desde el correo). Solo si se le avisó de esa alerta y el seguimiento sigue vigente.
     */
    @Transactional
    public String enlaceSeguimiento(Long userId, Long alertId, LocalDateTime ahora) {
        ReceivedAlert alerta = recibidas(userId, ahora).stream()
                .filter(a -> a.alertId().equals(alertId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("La alerta no existe"));
        if (!alerta.trackingAvailable()) {
            throw new ResourceNotFoundException("El seguimiento de esta alerta ya terminó");
        }
        return trackingLinkService.crear(alertId);
    }

    private ReceivedAlert aVista(AlertSummary a, LocalDateTime ahora) {
        String nombre = identityApi.findUserByProfileId(a.userProfileId())
                .map(u -> u.firstName() == null ? "" : u.firstName().trim())
                .orElse("");
        LocalDateTime fin = a.endedAt() != null ? a.endedAt() : a.cancelledAt();
        boolean activa = "active".equals(a.status());
        boolean seguimiento = activa || (fin != null && fin.plus(TrackingLinkService.VIGENCIA_TRAS_TERMINAR).isAfter(ahora));
        return new ReceivedAlert(a.id(), nombre, a.status(), activa, a.startedAt(), fin, seguimiento);
    }

    /** Lo que ve el contacto: solo el nombre de pila de quien activó la alerta. */
    public record ReceivedAlert(Long alertId, String firstName, String status, boolean active,
                                LocalDateTime startedAt, LocalDateTime endedAt, boolean trackingAvailable) {}
}
