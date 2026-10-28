package com.alertamujer.microservice;

import com.alertamujer.devices.api.NewPhoneSignedInEvent;
import com.alertamujer.identity.api.AccountDeletedEvent;
import com.alertamujer.identity.api.PasswordRecoveryRequestedEvent;
import com.alertamujer.shared.rpc.RemoteEvents;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Emails and pushes are sent by Alert Service (notifications module). These events are born here,
 * so they are forwarded over HTTP once the transaction commits, in the background. Account deletion
 * also goes to Evidence Service.
 */
@Component
class EventsToAlertService {

    private final RemoteEvents alerts;
    private final RemoteEvents evidences;

    EventsToAlertService(@Qualifier("eventsToAlerts") RemoteEvents alerts,
                             @Qualifier("eventsToEvidence") RemoteEvents evidences) {
        this.alerts = alerts;
        this.evidences = evidences;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void passwordRecovery(PasswordRecoveryRequestedEvent event) {
        alerts.send(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void newPhone(NewPhoneSignedInEvent event) {
        alerts.send(event);
    }

    /** Cuenta eliminada: Alert borra contactos, ubicaciones y recorridos; Evidence, las grabaciones. */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void accountDeleted(AccountDeletedEvent event) {
        alerts.send(event);
        evidences.send(event);
    }
}
