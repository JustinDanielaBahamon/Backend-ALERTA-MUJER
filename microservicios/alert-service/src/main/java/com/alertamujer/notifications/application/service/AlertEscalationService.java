package com.alertamujer.notifications.application.service;

import com.alertamujer.notifications.application.port.in.EscalateAlertsUseCase;
import com.alertamujer.notifications.application.port.in.NotifyContactsUseCase;
import com.alertamujer.notifications.application.port.out.EscalationRepository;
import com.alertamujer.notifications.application.port.out.EscalationRepository.UnseenAlert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * If nobody opened the tracking of an active alert after a few minutes (the email went to spam, the
 * phone was silent...), the contacts are notified again. Only once per alert, and only for recent
 * alerts so a server that was asleep does not resend old ones.
 */
@Service
public class AlertEscalationService implements EscalateAlertsUseCase {

    private static final Logger log = LoggerFactory.getLogger(AlertEscalationService.class);
    /** Older alerts are not escalated (e.g. after the server was asleep for hours). */
    static final Duration MAX_AGE = Duration.ofMinutes(60);

    private final EscalationRepository escalations;
    private final NotifyContactsUseCase notices;
    private final Duration after;

    public AlertEscalationService(EscalationRepository escalations, NotifyContactsUseCase notices,
                                  @Value("${alerts.escalation.after-minutes:5}") long afterMinutes) {
        this.escalations = escalations;
        this.notices = notices;
        this.after = Duration.ofMinutes(afterMinutes);
    }

    @Override
    public int escalate(LocalDateTime now) {
        int escalated = 0;
        for (UnseenAlert alert : escalations.unseen(now.minus(MAX_AGE), now.minus(after))) {
            // The mark goes first: if two instances run at once, only one resends
            if (!escalations.claim(alert.alertId(), now)) continue;
            try {
                notices.alertReminder(alert.alertId(), alert.userProfileId(), alert.startedAt());
                escalated++;
            } catch (RuntimeException e) {
                log.error("No se pudo reenviar el aviso de la alerta {}: {}", alert.alertId(), e.getMessage());
            }
        }
        if (escalated > 0) log.info("Avisos reenviados: {} alertas sin seguimiento abierto", escalated);
        return escalated;
    }
}
