package com.alertamujer.alerts.adapter.out.eventos;

import com.alertamujer.alerts.api.AlertActivatedEvent;
import com.alertamujer.alerts.api.AlertEndedEvent;
import com.alertamujer.alerts.application.port.out.EventosAlertaPort;
import com.alertamujer.alerts.domain.model.Alert;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Adaptador de salida: publica los eventos de Spring que escucha el módulo de notificaciones
 * (@TransactionalEventListener, después de confirmar la transacción y en otro hilo).
 */
@Component
class EventosAlertaAdapter implements EventosAlertaPort {

    private final ApplicationEventPublisher publisher;

    EventosAlertaAdapter(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void alertaActivada(Alert alerta) {
        publisher.publishEvent(new AlertActivatedEvent(alerta.getId(), alerta.getUserProfileId(), alerta.getStartedAt()));
    }

    @Override
    public void alertaTerminada(Alert alerta, LocalDateTime cuando) {
        publisher.publishEvent(new AlertEndedEvent(alerta.getId(), alerta.getUserProfileId(), alerta.getStatus(), cuando));
    }
}
