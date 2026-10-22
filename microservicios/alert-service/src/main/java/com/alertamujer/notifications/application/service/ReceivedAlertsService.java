package com.alertamujer.notifications.application.service;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.notifications.application.port.in.ReceivedAlertsUseCase;
import com.alertamujer.notifications.application.port.in.PublicTrackingUseCase;
import com.alertamujer.notifications.application.port.out.AlertsPort;
import com.alertamujer.notifications.application.port.out.IdentityPort;
import com.alertamujer.notifications.domain.model.TrackingLink;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Alerts of the people who have the user as an emergency contact. */
@Service
public class ReceivedAlertsService implements ReceivedAlertsUseCase {

    static final Duration RECENT = Duration.ofDays(30);

    private final AlertsPort alerts;
    private final IdentityPort identity;
    private final PublicTrackingUseCase tracking;

    public ReceivedAlertsService(AlertsPort alerts, IdentityPort identity, PublicTrackingUseCase tracking) {
        this.alerts = alerts;
        this.identity = identity;
        this.tracking = tracking;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceivedAlert> received(Long userId, LocalDateTime now) {
        String email = identity.emailOf(userId).orElse(null);
        Long myProfile = identity.profileOf(userId).orElse(null);
        return alerts.findMany(alerts.notifiedTo(email)).stream()
                .filter(a -> !Objects.equals(a.userProfileId(), myProfile)) // her own ones are not "received"
                .filter(a -> a.startedAt() != null && a.startedAt().isAfter(now.minus(RECENT)))
                .sorted(Comparator.comparing(AlertSummary::startedAt).reversed())
                .map(a -> view(a, now))
                .toList();
    }

    @Override
    @Transactional
    public String trackingLink(Long userId, Long alertId, LocalDateTime now) {
        ReceivedAlert alert = received(userId, now).stream()
                .filter(a -> a.alertId().equals(alertId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("La alerta no existe"));
        if (!alert.trackingAvailable()) {
            throw new ResourceNotFoundException("El seguimiento de esta alerta ya terminó");
        }
        return tracking.createLink(alertId);
    }

    private ReceivedAlert view(AlertSummary a, LocalDateTime now) {
        String name = identity.profileFirstName(a.userProfileId()).orElse("");
        LocalDateTime end = PublicTrackingService.end(a);
        boolean active = "active".equals(a.status());
        // Here an ended alert without an end time no longer offers tracking
        boolean available = active || (end != null && TrackingLink.liveTracking(a.status(), end, now));
        return new ReceivedAlert(a.id(), name, a.status(), active, a.startedAt(), end, available);
    }
}
