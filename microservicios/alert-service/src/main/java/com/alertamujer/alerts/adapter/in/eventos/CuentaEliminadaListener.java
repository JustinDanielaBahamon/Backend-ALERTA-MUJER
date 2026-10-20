package com.alertamujer.alerts.adapter.in.eventos;

import com.alertamujer.alerts.application.port.in.BorrarDatosDeUsuariaUseCase;
import com.alertamujer.identity.api.AccountDeletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Adaptador de entrada: Auth Service avisa por HTTP que se eliminó una cuenta. */
@Component
class CuentaEliminadaListener {

    private final BorrarDatosDeUsuariaUseCase borrado;

    CuentaEliminadaListener(BorrarDatosDeUsuariaUseCase borrado) {
        this.borrado = borrado;
    }

    @EventListener
    public void alEliminarCuenta(AccountDeletedEvent e) {
        borrado.borrarDatosDe(e.userProfileId());
    }
}
