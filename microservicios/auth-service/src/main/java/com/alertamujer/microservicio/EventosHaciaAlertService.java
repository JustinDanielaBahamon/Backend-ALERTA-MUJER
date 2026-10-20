package com.alertamujer.microservicio;

import com.alertamujer.devices.api.NewPhoneSignedInEvent;
import com.alertamujer.identity.api.PasswordRecoveryRequestedEvent;
import com.alertamujer.shared.rpc.EventosRemotos;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Los correos y los push los envía Alert Service (módulo notifications). Estos eventos nacen aquí,
 * así que se reenvían por HTTP una vez confirmada la transacción, en segundo plano.
 */
@Component
class EventosHaciaAlertService {

    private final EventosRemotos alertas;

    EventosHaciaAlertService(EventosRemotos alertas) {
        this.alertas = alertas;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void recuperacionDeContrasena(PasswordRecoveryRequestedEvent evento) {
        alertas.enviar(evento);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void celularNuevo(NewPhoneSignedInEvent evento) {
        alertas.enviar(evento);
    }
}
