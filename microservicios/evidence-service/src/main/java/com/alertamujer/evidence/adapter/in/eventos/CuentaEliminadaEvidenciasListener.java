package com.alertamujer.evidence.adapter.in.eventos;

import com.alertamujer.evidence.application.port.in.LimpiarEvidenciasUseCase;
import com.alertamujer.identity.api.AccountDeletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Adaptador de entrada: Auth Service avisa por HTTP que se eliminó una cuenta. */
@Component
class CuentaEliminadaEvidenciasListener {

    private final LimpiarEvidenciasUseCase limpieza;

    CuentaEliminadaEvidenciasListener(LimpiarEvidenciasUseCase limpieza) {
        this.limpieza = limpieza;
    }

    @EventListener
    public void alEliminarCuenta(AccountDeletedEvent e) {
        limpieza.borrarDeUsuaria(e.userProfileId());
    }
}
