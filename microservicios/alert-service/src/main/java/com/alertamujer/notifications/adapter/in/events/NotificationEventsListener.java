package com.alertamujer.notifications.adapter.in.events;

import com.alertamujer.alerts.api.AlertActivatedEvent;
import com.alertamujer.alerts.api.AlertEndedEvent;
import com.alertamujer.alerts.api.AlertWarningEvent;
import com.alertamujer.devices.api.NewPhoneSignedInEvent;
import com.alertamujer.identity.api.PasswordRecoveryRequestedEvent;
import com.alertamujer.notifications.application.port.in.NotifyContactsUseCase;
import com.alertamujer.notifications.application.port.in.NotifyNewPhoneUseCase;
import com.alertamujer.notifications.application.port.in.SendRecoveryCodeUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Input adapter: listens to the alerts events and the ones Auth Service forwards over HTTP. Runs
 * on another thread and only after the publishing transaction commits (if any), so it does not
 * notify about an alert that was never saved nor delay the response to the user.
 */
@Component
class NotificationEventsListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventsListener.class);

    private final NotifyContactsUseCase notices;
    private final NotifyNewPhoneUseCase newPhone;
    private final SendRecoveryCodeUseCase recoveryCode;

    NotificationEventsListener(NotifyContactsUseCase notices, NotifyNewPhoneUseCase newPhone,
                                  SendRecoveryCodeUseCase recoveryCode) {
        this.notices = notices;
        this.newPhone = newPhone;
        this.recoveryCode = recoveryCode;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onAlertActivated(AlertActivatedEvent e) {
        try {
            notices.alertActivated(e.alertId(), e.userProfileId(), e.startedAt());
        } catch (RuntimeException ex) {
            log.error("No se pudo avisar a los contactos de la alerta {}: {}", e.alertId(), ex.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onAlertEnded(AlertEndedEvent e) {
        try {
            notices.alertEnded(e.alertId(), e.userProfileId(), e.status(), e.endedAt());
        } catch (RuntimeException ex) {
            log.error("No se pudo avisar el fin de la alerta {}: {}", e.alertId(), ex.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onAlertWarning(AlertWarningEvent e) {
        try {
            notices.alertWarning(e.alertId(), e.userProfileId(), e.kind(), e.at());
        } catch (RuntimeException ex) {
            log.error("No se pudo avisar a los contactos ({}) de la alerta {}: {}", e.kind(), e.alertId(), ex.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onNewPhoneSignedIn(NewPhoneSignedInEvent e) {
        newPhone.notify(e.otherTokens(), e.phone());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onRecoveryRequested(PasswordRecoveryRequestedEvent e) {
        recoveryCode.send(e.email(), e.name(), e.code(), e.validityMinutes());
    }
}
