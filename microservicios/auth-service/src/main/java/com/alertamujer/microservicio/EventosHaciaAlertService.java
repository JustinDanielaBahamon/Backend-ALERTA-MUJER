package com.alertamujer.microservicio;

import com.alertamujer.devices.api.NewPhoneSignedInEvent;
import com.alertamujer.identity.api.AccountDeletedEvent;
import com.alertamujer.identity.api.PasswordRecoveryRequestedEvent;
import com.alertamujer.shared.rpc.EventosRemotos;
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

    private final EventosRemotos alertas;
    private final EventosRemotos evidencias;

    EventosHaciaAlertService(@Qualifier("eventosHaciaAlertas") EventosRemotos alertas,
                             @Qualifier("eventosHaciaEvidencias") EventosRemotos evidencias) {
        this.alertas = alertas;
        this.evidencias = evidencias;
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

    /** Cuenta eliminada: Alert borra contactos, ubicaciones y recorridos; Evidence, las grabaciones. */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void cuentaEliminada(AccountDeletedEvent evento) {
        alertas.enviar(evento);
        evidencias.enviar(evento);
    }
}
