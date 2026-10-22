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
 * Los correos y los push los envía Alert Service (módulo notifications). Estos eventos nacen aquí,
 * así que se reenvían por HTTP una vez confirmada la transacción, en segundo plano. La eliminación
 * de una cuenta también va a Evidence Service.
 */
@Component
class EventosHaciaAlertService {

    private final RemoteEvents alerts;
    private final RemoteEvents evidences;

    EventosHaciaAlertService(@Qualifier("eventosHaciaAlertas") RemoteEvents alerts,
                             @Qualifier("eventosHaciaEvidencias") RemoteEvents evidences) {
        this.alerts = alerts;
        this.evidences = evidences;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void recuperacionDeContrasena(PasswordRecoveryRequestedEvent event) {
        alerts.send(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void celularNuevo(NewPhoneSignedInEvent event) {
        alerts.send(event);
    }

    /** Cuenta eliminada: Alert borra contactos, ubicaciones y recorridos; Evidence, las grabaciones. */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void cuentaEliminada(AccountDeletedEvent event) {
        alerts.send(event);
        evidences.send(event);
    }
}
