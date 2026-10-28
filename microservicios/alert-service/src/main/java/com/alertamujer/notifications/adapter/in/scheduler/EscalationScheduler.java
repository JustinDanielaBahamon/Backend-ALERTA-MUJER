package com.alertamujer.notifications.adapter.in.scheduler;

import com.alertamujer.notifications.application.port.in.EscalateAlertsUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Input adapter: every minute looks for active alerts whose tracking nobody opened. */
@Component
class EscalationScheduler {

    private final EscalateAlertsUseCase escalation;

    EscalationScheduler(EscalateAlertsUseCase escalation) {
        this.escalation = escalation;
    }

    @Scheduled(fixedDelayString = "${alerts.escalation.check-ms:60000}", initialDelay = 20000)
    void escalate() {
        escalation.escalate(LocalDateTime.now());
    }
}
