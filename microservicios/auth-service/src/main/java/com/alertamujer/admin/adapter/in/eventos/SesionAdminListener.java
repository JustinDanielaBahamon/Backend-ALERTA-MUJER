package com.alertamujer.admin.adapter.in.eventos;

import com.alertamujer.admin.application.port.in.RegistrarEventoSeguridadUseCase;
import com.alertamujer.identity.api.AdminSessionEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada: escucha los eventos de sesión que publica identity. Es síncrono para que el
 * intento fallido quede registrado aunque el login termine en error.
 */
@Component
class SesionAdminListener {

    private final RegistrarEventoSeguridadUseCase eventos;

    SesionAdminListener(RegistrarEventoSeguridadUseCase eventos) {
        this.eventos = eventos;
    }

    @EventListener
    public void alEventoDeSesion(AdminSessionEvent evento) {
        eventos.registrar(evento.accountId(), evento.action(), evento.detail());
    }
}
