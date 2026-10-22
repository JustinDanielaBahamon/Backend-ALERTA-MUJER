package com.alertamujer.alerts.adapter.out.events;

import com.alertamujer.alerts.api.AlertActivatedEvent;
import com.alertamujer.alerts.api.AlertEndedEvent;
import com.alertamujer.alerts.application.port.out.AlertEventsPort;
import com.alertamujer.alerts.domain.model.Alert;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Adaptador de salida: publica los eventos de Spring que escucha el módulo de notificaciones
 * (@TransactionalEventListener, después de confirmar la transacción y en otro hilo).
 */
@Component
class AlertEventsAdapter implements AlertEventsPort {

    private final ApplicationEventPublisher publisher;

    AlertEventsAdapter(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void alertActivated(Alert alert) {
        publisher.publishEvent(new AlertActivatedEvent(alert.getId(), alert.getUserProfileId(), alert.getStartedAt()));
    }

    @Override
    public void alertEnded(Alert alert, LocalDateTime when) {
        publisher.publishEvent(new AlertEndedEvent(alert.getId(), alert.getUserProfileId(), alert.getStatus(), when));
    }
}
