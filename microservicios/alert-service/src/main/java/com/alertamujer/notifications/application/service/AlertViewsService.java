package com.alertamujer.notifications.application.service;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.notifications.application.port.in.AlertViewsUseCase;
import com.alertamujer.notifications.application.port.out.AlertsPort;
import com.alertamujer.notifications.application.port.out.IdentityPort;
import com.alertamujer.notifications.application.port.out.ViewsRepository;
import com.alertamujer.notifications.application.port.out.ViewsRepository.ViewCount;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/** Tracking views: only the alert owner can query them. */
@Service
public class AlertViewsService implements AlertViewsUseCase {

    /** Tracking refreshes every few seconds: whoever visited less than this ago is watching it. */
    static final Duration WATCHING_NOW = Duration.ofSeconds(90);

    private final AlertsPort alerts;
    private final IdentityPort identity;
    private final ViewsRepository views;

    public AlertViewsService(AlertsPort alerts, IdentityPort identity, ViewsRepository views) {
        this.alerts = alerts;
        this.identity = identity;
        this.views = views;
    }

    @Override
    public ViewCount views(Long alertId, Long userId, LocalDateTime now) {
        AlertSummary alert = alerts.find(alertId).orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        Long profile = identity.profileOf(userId).orElse(null);
        // Someone else's alert is answered like a missing one: its existence is not revealed
        if (!Objects.equals(profile, alert.userProfileId())) throw new ResourceNotFoundException("Alert", alertId);
        return views.count(alertId, now.minus(WATCHING_NOW));
    }
}
