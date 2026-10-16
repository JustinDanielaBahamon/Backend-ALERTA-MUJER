package com.alertamujer.notifications.application;

import com.alertamujer.alerts.api.AlertActivatedEvent;
import com.alertamujer.alerts.api.AlertRecipient;
import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.config.FcmService;
import com.alertamujer.devices.api.DevicesApi;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.notifications.application.email.EmailSender;
import com.alertamujer.settings.api.SettingsApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Avisa a los contactos de emergencia cuando una usuaria activa una alerta (RF4.2): correo con el
 * enlace de seguimiento y push de alta prioridad a quienes tienen la app. Corre en segundo plano,
 * después de guardar la alerta, y deja el resultado de cada envío en la alerta (sent / failed).
 */
@Service
public class AlertNotificationService {

    private static final Logger log = LoggerFactory.getLogger(AlertNotificationService.class);

    private final AlertsApi alertsApi;
    private final IdentityApi identityApi;
    private final DevicesApi devicesApi;
    private final SettingsApi settingsApi;
    private final TrackingLinkService trackingLinkService;
    private final EmailSender emailSender;
    private final FcmService fcmService;
    private final String webBaseUrl;

    public AlertNotificationService(AlertsApi alertsApi, IdentityApi identityApi, DevicesApi devicesApi,
                                    SettingsApi settingsApi, TrackingLinkService trackingLinkService,
                                    EmailSender emailSender, FcmService fcmService,
                                    @Value("${notifications.web-base-url:http://localhost:4200}") String webBaseUrl) {
        this.alertsApi = alertsApi;
        this.identityApi = identityApi;
        this.devicesApi = devicesApi;
        this.settingsApi = settingsApi;
        this.trackingLinkService = trackingLinkService;
        this.emailSender = emailSender;
        this.fcmService = fcmService;
        this.webBaseUrl = webBaseUrl.replaceAll("/+$", "");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alActivarAlerta(AlertActivatedEvent evento) {
        try {
            avisar(evento);
        } catch (RuntimeException e) {
            log.error("No se pudo avisar a los contactos de la alerta {}: {}", evento.alertId(), e.getMessage());
        }
    }

    void avisar(AlertActivatedEvent evento) {
        List<AlertRecipient> envios = alertsApi.recipientsOf(evento.alertId());
        if (envios.isEmpty()) return;
        Optional<AlertSummary> alerta = alertsApi.findAlert(evento.alertId());
        String nombre = identityApi.findUserByProfileId(evento.userProfileId()).map(UserSummary::fullName).orElse(null);

        // El correo de cada contacto sirve también para saber si tiene la app (push)
        Map<Long, String> correoPorContacto = envios.stream()
                .filter(e -> AlertsChannels.EMAIL.equals(e.channel()))
                .collect(Collectors.toMap(AlertRecipient::emergencyContactId, AlertRecipient::destination, (a, b) -> a));

        // Un enlace por alerta: lo usan el correo (página web) y el push (pantalla de la app)
        String codigo = trackingLinkService.crear(evento.alertId());
        String seguimiento = webBaseUrl + "/seguimiento/" + codigo;
        for (AlertRecipient envio : envios) {
            if (AlertsChannels.EMAIL.equals(envio.channel())) {
                enviarCorreo(envio, nombre, evento, alerta.orElse(null), seguimiento);
            } else if (AlertsChannels.PUSH.equals(envio.channel())) {
                enviarPush(envio, correoPorContacto.get(envio.emergencyContactId()), nombre, evento, codigo);
            }
        }
    }

    private void enviarCorreo(AlertRecipient envio, String nombre, AlertActivatedEvent evento, AlertSummary alerta,
                              String seguimiento) {
        String maps = alerta != null && alerta.latitude() != null && alerta.longitude() != null
                ? TrackingLinkService.googleMaps(alerta.latitude(), alerta.longitude()) : null;
        try {
            emailSender.send(AlertEmailTemplate.construir(envio.destination(), envio.contactName(), nombre,
                    evento.startedAt(), seguimiento, maps, settingsApi.numeroEmergencia()));
            alertsApi.markDelivery(envio.id(), "sent", null);
        } catch (EmailSender.EmailException e) {
            alertsApi.markDelivery(envio.id(), "failed", e.getMessage());
        }
    }

    private void enviarPush(AlertRecipient envio, String correoContacto, String nombre, AlertActivatedEvent evento,
                            String codigo) {
        List<String> tokens = identityApi.findUserByEmail(correoContacto)
                .map(u -> devicesApi.pushTokensOfUser(u.id()))
                .orElse(List.of());
        if (tokens.isEmpty()) {
            alertsApi.markDelivery(envio.id(), "failed", "El contacto no tiene la app con notificaciones activas");
            return;
        }
        List<String> fallidos = fcmService.sendEmergencyAlert(tokens, Map.of(
                "tipo", "ALERTA_SOS",
                "alertaId", String.valueOf(evento.alertId()),
                "nombre", nombre == null ? "" : nombre,
                "codigo", codigo));
        if (fallidos.size() == tokens.size()) {
            alertsApi.markDelivery(envio.id(), "failed", "FCM rechazó el envío a todos los dispositivos");
        } else {
            alertsApi.markDelivery(envio.id(), "sent", null);
        }
    }

    /** Canales que registra el módulo alerts para cada contacto. */
    static final class AlertsChannels {
        static final String EMAIL = "email";
        static final String PUSH = "push";

        private AlertsChannels() {}
    }
}
