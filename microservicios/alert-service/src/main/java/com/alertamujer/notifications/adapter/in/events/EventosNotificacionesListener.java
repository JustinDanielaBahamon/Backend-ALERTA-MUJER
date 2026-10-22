package com.alertamujer.notifications.adapter.in.events;

import com.alertamujer.alerts.api.AlertActivatedEvent;
import com.alertamujer.alerts.api.AlertEndedEvent;
import com.alertamujer.devices.api.NewPhoneSignedInEvent;
import com.alertamujer.identity.api.PasswordRecoveryRequestedEvent;
import com.alertamujer.notifications.application.port.in.AvisarContactosUseCase;
import com.alertamujer.notifications.application.port.in.AvisarNuevoCelularUseCase;
import com.alertamujer.notifications.application.port.in.EnviarCodigoRecuperacionUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Adaptador de entrada: escucha los eventos de alerts y los que reenvía Auth Service por HTTP. Corre
 * en otro hilo y solo después de confirmar la transacción que los publicó (si la hay), para no
 * avisar de una alerta que no llegó a guardarse ni demorar la respuesta a la usuaria.
 */
@Component
class EventosNotificacionesListener {

    private static final Logger log = LoggerFactory.getLogger(EventosNotificacionesListener.class);

    private final AvisarContactosUseCase avisos;
    private final AvisarNuevoCelularUseCase newPhone;
    private final EnviarCodigoRecuperacionUseCase recoveryCode;

    EventosNotificacionesListener(AvisarContactosUseCase avisos, AvisarNuevoCelularUseCase newPhone,
                                  EnviarCodigoRecuperacionUseCase recoveryCode) {
        this.avisos = avisos;
        this.newPhone = newPhone;
        this.recoveryCode = recoveryCode;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alActivarAlerta(AlertActivatedEvent e) {
        try {
            avisos.alertaActivada(e.alertId(), e.userProfileId(), e.startedAt());
        } catch (RuntimeException ex) {
            log.error("No se pudo avisar a los contactos de la alerta {}: {}", e.alertId(), ex.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alTerminarAlerta(AlertEndedEvent e) {
        try {
            avisos.alertaTerminada(e.alertId(), e.userProfileId(), e.status(), e.endedAt());
        } catch (RuntimeException ex) {
            log.error("No se pudo avisar el fin de la alerta {}: {}", e.alertId(), ex.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alEntrarDesdeOtroCelular(NewPhoneSignedInEvent e) {
        newPhone.avisar(e.otherTokens(), e.phone());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alPedirRecuperacion(PasswordRecoveryRequestedEvent e) {
        recoveryCode.send(e.email(), e.name(), e.code(), e.validityMinutes());
    }
}
