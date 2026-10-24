package com.alertamujer.notifications.application.service;

import com.alertamujer.alerts.api.AlertRecipient;
import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.notifications.application.port.in.NotifyContactsUseCase;
import com.alertamujer.notifications.application.port.in.PublicTrackingUseCase;
import com.alertamujer.notifications.application.port.out.SettingsPort;
import com.alertamujer.notifications.application.port.out.AlertsPort;
import com.alertamujer.notifications.application.port.out.EmailsPort;
import com.alertamujer.notifications.application.port.out.EmailsPort.EmailNotSentException;
import com.alertamujer.notifications.application.port.out.DevicesPort;
import com.alertamujer.notifications.application.port.out.IdentityPort;
import com.alertamujer.notifications.application.port.out.PushPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Notices to the emergency contacts when an alert is activated and when it ends. Only knows ports. */
@Service
public class AlertNoticesService implements NotifyContactsUseCase {

    private static final Logger log = LoggerFactory.getLogger(AlertNoticesService.class);

    /** Channels the alerts module records for each contact. */
    static final String EMAIL_CHANNEL = "email";
    static final String PUSH_CHANNEL = "push";
    /** Alerts the server activates for her ("Avísame si no llego"): their message explains why. */
    static final Set<String> SERVER_METHODS = Set.of("timer", "signal_lost");

    private final AlertsPort alerts;
    private final IdentityPort identity;
    private final DevicesPort devices;
    private final SettingsPort settings;
    private final PublicTrackingUseCase tracking;
    private final EmailsPort emails;
    private final PushPort push;
    private final String webBaseUrl;

    public AlertNoticesService(AlertsPort alerts, IdentityPort identity, DevicesPort devices,
                               SettingsPort settings, PublicTrackingUseCase tracking, EmailsPort emails,
                               PushPort push, @Value("${notifications.web-base-url:http://localhost:4200}") String webBaseUrl) {
        this.alerts = alerts;
        this.identity = identity;
        this.devices = devices;
        this.settings = settings;
        this.tracking = tracking;
        this.emails = emails;
        this.push = push;
        this.webBaseUrl = webBaseUrl.replaceAll("/+$", "");
    }

    @Override
    public void alertActivated(Long alertId, Long userProfile, LocalDateTime start) {
        List<AlertRecipient> deliveries = alerts.recipients(alertId);
        if (deliveries.isEmpty()) return;
        AlertSummary alert = alerts.find(alertId).orElse(null);
        String name = identity.profileFullName(userProfile).orElse(null);
        Map<Long, String> emailByContact = emailsByContact(deliveries);
        // One link per alert: used by the email (web page) and the push (app screen)
        String code = tracking.createLink(alertId);
        String url = webBaseUrl + "/seguimiento/" + code;
        for (AlertRecipient delivery : deliveries) {
            if (EMAIL_CHANNEL.equals(delivery.channel())) {
                sendEmail(delivery, name, start, alert, url);
            } else if (PUSH_CHANNEL.equals(delivery.channel())) {
                sendPush(delivery, emailByContact.get(delivery.emergencyContactId()), name, alertId, code);
            }
        }
    }

    @Override
    public void alertReminder(Long alertId, Long userProfile, LocalDateTime start) {
        List<AlertRecipient> deliveries = alerts.recipients(alertId);
        if (deliveries.isEmpty()) return;
        AlertSummary alert = alerts.find(alertId).orElse(null);
        String name = identity.profileFullName(userProfile).orElse(null);
        Map<Long, String> emailByContact = emailsByContact(deliveries);
        String code = tracking.createLink(alertId);
        String url = webBaseUrl + "/seguimiento/" + code;
        String reason = alert == null ? null : serverReason(alert);
        for (AlertRecipient delivery : deliveries) {
            try {
                if (EMAIL_CHANNEL.equals(delivery.channel())) {
                    emails.alertReminder(delivery.destination(), delivery.contactName(), name, start, url, reason,
                            settings.emergencyNumber());
                } else if (PUSH_CHANNEL.equals(delivery.channel()) && delivery.emergencyContactId() != null) {
                    List<String> tokens = contactTokens(emailByContact.get(delivery.emergencyContactId()));
                    if (!tokens.isEmpty()) {
                        push.send(tokens, Map.of(
                                "tipo", "ALERTA_SOS",
                                "alertaId", String.valueOf(alertId),
                                "nombre", name == null ? "" : name,
                                "codigo", code,
                                "recordatorio", "1"));
                    }
                }
            } catch (EmailNotSentException e) {
                log.warn("No se pudo reenviar el aviso de la alerta {} al envío {}: {}", alertId, delivery.id(), e.getMessage());
            }
        }
    }

    /** The explanation the server wrote when it activated the alert for her (trips); null otherwise. */
    static String serverReason(AlertSummary alert) {
        return SERVER_METHODS.contains(alert.activationMethod()) ? alert.message() : null;
    }

    @Override
    public void alertEnded(Long alertId, Long userProfile, String status, LocalDateTime end) {
        List<AlertRecipient> deliveries = alerts.recipients(alertId);
        if (deliveries.isEmpty()) return;
        String name = identity.profileFullName(userProfile).orElse(null);
        Map<Long, String> emailByContact = emailsByContact(deliveries);
        for (AlertRecipient delivery : deliveries) {
            try {
                if (EMAIL_CHANNEL.equals(delivery.channel())) {
                    emails.alertEnd(delivery.destination(), delivery.contactName(), name, status, end, settings.emergencyNumber());
                } else if (PUSH_CHANNEL.equals(delivery.channel()) && delivery.emergencyContactId() != null) {
                    List<String> tokens = contactTokens(emailByContact.get(delivery.emergencyContactId()));
                    if (!tokens.isEmpty()) {
                        push.send(tokens, Map.of(
                                "tipo", "ALERTA_FINALIZADA",
                                "alertaId", String.valueOf(alertId),
                                "nombre", name == null ? "" : name,
                                "estado", status));
                    }
                }
            } catch (EmailNotSentException e) {
                log.warn("No se pudo enviar el aviso de fin de la alerta {} al envío {}: {}", alertId, delivery.id(), e.getMessage());
            }
        }
    }

    /** Google Maps link with the point (for the email, as a fallback if the page is slow). */
    static String googleMaps(BigDecimal lat, BigDecimal lng) {
        return "https://www.google.com/maps/search/?api=1&query=" + lat.toPlainString() + "," + lng.toPlainString();
    }

    private void sendEmail(AlertRecipient delivery, String name, LocalDateTime start, AlertSummary alert, String url) {
        String maps = alert != null && alert.latitude() != null && alert.longitude() != null
                ? googleMaps(alert.latitude(), alert.longitude()) : null;
        try {
            emails.alertNotice(delivery.destination(), delivery.contactName(), name, start, url, maps, settings.emergencyNumber());
            alerts.recordDelivery(delivery.id(), "sent", null);
        } catch (EmailNotSentException e) {
            alerts.recordDelivery(delivery.id(), "failed", e.getMessage());
        }
    }

    private void sendPush(AlertRecipient delivery, String contactEmail, String name, Long alertId, String code) {
        List<String> tokens = contactTokens(contactEmail);
        if (tokens.isEmpty()) {
            alerts.recordDelivery(delivery.id(), "failed", "El contacto no tiene la app con notificaciones activas");
            return;
        }
        List<String> failedOnes = push.send(tokens, Map.of(
                "tipo", "ALERTA_SOS",
                "alertaId", String.valueOf(alertId),
                "nombre", name == null ? "" : name,
                "codigo", code));
        if (failedOnes.size() == tokens.size()) {
            alerts.recordDelivery(delivery.id(), "failed", "FCM rechazó el envío a todos los dispositivos");
        } else {
            alerts.recordDelivery(delivery.id(), "sent", null);
        }
    }

    // Each contact's email also tells whether she has the app (push)
    private static Map<Long, String> emailsByContact(List<AlertRecipient> deliveries) {
        return deliveries.stream()
                .filter(e -> EMAIL_CHANNEL.equals(e.channel()))
                .collect(Collectors.toMap(AlertRecipient::emergencyContactId, AlertRecipient::destination, (a, b) -> a));
    }

    private List<String> contactTokens(String email) {
        return identity.userWithEmail(email).map(devices::tokensOf).orElse(List.of());
    }
}
