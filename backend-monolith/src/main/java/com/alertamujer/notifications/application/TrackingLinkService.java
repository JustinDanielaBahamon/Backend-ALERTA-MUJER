package com.alertamujer.notifications.application;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.evidence.api.EvidenceApi;
import com.alertamujer.alerts.api.LocationPoint;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.notifications.domain.model.TrackingLink;
import com.alertamujer.notifications.infrastructure.repository.TrackingLinkRepository;
import com.alertamujer.settings.api.SettingsApi;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

/**
 * Enlaces públicos para seguir una alerta desde el correo. El código tiene 256 bits aleatorios y
 * solo viaja en el correo; en la base queda su hash. El enlace vale mientras la alerta está activa
 * y hasta {@link #VIGENCIA_TRAS_TERMINAR} después de que termina.
 */
@Service
public class TrackingLinkService {

    static final Duration VIGENCIA_TRAS_TERMINAR = Duration.ofHours(2);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TrackingLinkRepository repository;
    private final AlertsApi alertsApi;
    private final IdentityApi identityApi;
    private final SettingsApi settingsApi;
    private final EvidenceApi evidenceApi;

    public TrackingLinkService(TrackingLinkRepository repository, AlertsApi alertsApi, IdentityApi identityApi,
                               SettingsApi settingsApi, EvidenceApi evidenceApi) {
        this.repository = repository;
        this.alertsApi = alertsApi;
        this.identityApi = identityApi;
        this.settingsApi = settingsApi;
        this.evidenceApi = evidenceApi;
    }

    /** Crea un enlace para la alerta y devuelve el código (no se puede recuperar después). */
    @Transactional
    public String crear(Long alertId) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String codigo = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        TrackingLink link = new TrackingLink();
        link.setAlertId(alertId);
        link.setTokenHash(hash(codigo));
        repository.save(link);
        return codigo;
    }

    /** Datos públicos de la alerta; ResourceNotFoundException si el enlace no existe, se revocó o venció. */
    @Transactional(readOnly = true)
    public TrackingView consultar(String codigo, LocalDateTime ahora) {
        AlertSummary alerta = alertaVigente(codigo, ahora);
        LocalDateTime fin = alerta.endedAt() != null ? alerta.endedAt() : alerta.cancelledAt();
        boolean activa = "active".equals(alerta.status());

        String nombre = identityApi.findUserByProfileId(alerta.userProfileId())
                .map(u -> u.firstName() == null ? "" : u.firstName().trim())
                .orElse("");
        List<LocationPoint> recorrido = new ArrayList<>(alertsApi.locationTrail(alerta.id()));
        if (recorrido.isEmpty() && alerta.latitude() != null && alerta.longitude() != null) {
            recorrido.add(new LocationPoint(alerta.latitude(), alerta.longitude(), null, alerta.startedAt()));
        }
        // Video y audio grabados durante la alerta: el contacto puede verlos para ayudar a ubicarla
        List<EvidenciaPublica> evidencias = evidenceApi.evidenciasDeAlerta(alerta.id()).stream()
                .map(e -> new EvidenciaPublica(e.id(), e.mediaType(), e.contentType(), e.createdAt()))
                .toList();
        return new TrackingView(nombre, alerta.status(), activa, alerta.startedAt(), fin, recorrido,
                settingsApi.numeroEmergencia(), evidencias);
    }

    /**
     * Archivo de una evidencia de la alerta del enlace, mientras el enlace siga vigente (alerta activa
     * o hasta 2 h después de terminar). ResourceNotFoundException si no corresponde.
     */
    @Transactional(readOnly = true)
    public EvidenceApi.EvidenceFile evidencia(String codigo, Long evidenciaId, LocalDateTime ahora) {
        AlertSummary alerta = alertaVigente(codigo, ahora);
        return evidenceApi.archivo(alerta.id(), evidenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("La evidencia no existe"));
    }

    private AlertSummary alertaVigente(String codigo, LocalDateTime ahora) {
        if (codigo == null || codigo.length() < 20 || codigo.length() > 100) throw noDisponible();
        TrackingLink link = repository.findByTokenHash(hash(codigo)).orElseThrow(TrackingLinkService::noDisponible);
        if (link.getRevokedAt() != null) throw noDisponible();
        AlertSummary alerta = alertsApi.findAlert(link.getAlertId()).orElseThrow(TrackingLinkService::noDisponible);

        LocalDateTime fin = alerta.endedAt() != null ? alerta.endedAt() : alerta.cancelledAt();
        boolean activa = "active".equals(alerta.status());
        if (!activa && fin != null && fin.plus(VIGENCIA_TRAS_TERMINAR).isBefore(ahora)) throw noDisponible();
        return alerta;
    }

    static String hash(String codigo) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(codigo.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static ResourceNotFoundException noDisponible() {
        return new ResourceNotFoundException("El enlace de seguimiento no existe o ya venció");
    }

    /** Solo el nombre de pila de la usuaria: el enlace puede reenviarse. */
    public record TrackingView(String firstName, String status, boolean active, LocalDateTime startedAt,
                               LocalDateTime endedAt, List<LocationPoint> route, String emergencyNumber,
                               List<EvidenciaPublica> evidence) {

        public LocationPoint lastPoint() {
            return route.isEmpty() ? null : route.get(route.size() - 1);
        }
    }

    /** Evidencia que ve el contacto: se abre con /api/public/tracking/{codigo}/evidence/{id}. */
    public record EvidenciaPublica(Long id, String mediaType, String contentType, LocalDateTime createdAt) {}

    /** Enlace de Google Maps con el punto (para el correo, como respaldo). */
    public static String googleMaps(BigDecimal lat, BigDecimal lng) {
        return "https://www.google.com/maps/search/?api=1&query=" + lat.toPlainString() + "," + lng.toPlainString();
    }
}
