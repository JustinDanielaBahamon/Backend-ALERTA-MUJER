package com.alertamujer.identity.adapter.out.eventos;

import com.alertamujer.identity.api.AdminSessionEvent;
import com.alertamujer.identity.api.PasswordRecoveryRequestedEvent;
import com.alertamujer.identity.application.port.out.EventosIdentidadPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida: eventos de Spring. AdminSessionEvent lo audita el módulo admin; el de
 * recuperación se reenvía por HTTP a Alert Service, que envía el correo.
 */
@Component
class EventosIdentidadAdapter implements EventosIdentidadPort {

    private final ApplicationEventPublisher publisher;

    EventosIdentidadAdapter(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void sesionAdmin(Long accountId, String accion, String detalle) {
        publisher.publishEvent(new AdminSessionEvent(accountId, accion, detalle));
    }

    @Override
    public void codigoRecuperacion(String correo, String nombre, String codigo, int minutosValidez) {
        publisher.publishEvent(new PasswordRecoveryRequestedEvent(correo, nombre, codigo, minutosValidez));
    }
}
