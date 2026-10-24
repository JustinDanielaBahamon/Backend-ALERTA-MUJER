package com.alertamujer.alerts.adapter.out.events;

import com.alertamujer.alerts.api.AlertActivatedEvent;
import com.alertamujer.alerts.api.AlertEndedEvent;
import com.alertamujer.alerts.api.AlertWarningEvent;
import com.alertamujer.alerts.application.port.out.AlertEventsPort;
import com.alertamujer.alerts.domain.model.Alert;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Output adapter: publishes the Spring events the notifications module listens to
 * (@TransactionalEventListener, after the transaction commits and on another thread).
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

    @Override
    public void alertWarning(Alert alert, String kind, LocalDateTime at) {
        publisher.publishEvent(new AlertWarningEvent(alert.getId(), alert.getUserProfileId(), kind, at));
    }
}
