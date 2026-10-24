package com.alertamujer.notifications.application.service;

import com.alertamujer.alerts.api.AlertMessageView;
import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.LocationPoint;
import com.alertamujer.evidence.api.EvidenceApi.EvidenceFile;
import com.alertamujer.notifications.application.port.in.PublicTrackingUseCase;
import com.alertamujer.notifications.application.port.out.SettingsPort;
import com.alertamujer.notifications.application.port.out.AlertsPort;
import com.alertamujer.notifications.application.port.out.LinkRepository;
import com.alertamujer.notifications.application.port.out.EvidencePort;
import com.alertamujer.notifications.application.port.out.IdentityPort;
import com.alertamujer.notifications.application.port.out.ViewsRepository;
import com.alertamujer.notifications.domain.model.ContactMessageLimit;
import com.alertamujer.notifications.domain.model.TrackingLink;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Tracking links and what the contact sees with them: location, recordings and chat. */
@Service
public class PublicTrackingService implements PublicTrackingUseCase {

    private final LinkRepository links;
    private final AlertsPort alerts;
    private final IdentityPort identity;
    private final SettingsPort settings;
    private final EvidencePort evidences;
    private final ViewsRepository views;
    private final ContactMessageLimit limit = new ContactMessageLimit();

    public PublicTrackingService(LinkRepository links, AlertsPort alerts, IdentityPort identity,
                                     SettingsPort settings, EvidencePort evidences, ViewsRepository views) {
        this.links = links;
        this.alerts = alerts;
        this.identity = identity;
        this.settings = settings;
        this.evidences = evidences;
        this.views = views;
    }

    @Override
    @Transactional
    public String createLink(Long alertId) {
        String code = TrackingLink.generateCode();
        links.save(TrackingLink.forAlert(alertId, code));
        return code;
    }

    @Override
    @Transactional(readOnly = true)
    public TrackingView query(String code, LocalDateTime now) {
        return view(liveAlert(code, now));
    }

    @Override
    @Transactional
    public TrackingView query(String code, String visitor, LocalDateTime now) {
        AlertSummary alert = liveAlert(code, now);
        if (visitor != null && visitor.matches("[A-Za-z0-9_-]{8,64}")) {
            views.registrar(alert.id(), visitor, now);
        }
        return view(alert);
    }

    @Override
    @Transactional
    public void reached(String code, Long recipientId, boolean seen, LocalDateTime now) {
        if (recipientId == null) return;
        try {
            alerts.markReached(liveAlert(code, now).id(), recipientId, seen);
        } catch (RuntimeException e) {
            // An expired link or someone else's code: there is nothing to mark
        }
    }

    private TrackingView view(AlertSummary alert) {
        String name = identity.profileFirstName(alert.userProfileId()).orElse("");
        List<LocationPoint> route = new ArrayList<>(alerts.route(alert.id()));
        if (route.isEmpty() && alert.latitude() != null && alert.longitude() != null) {
            route.add(new LocationPoint(alert.latitude(), alert.longitude(), null, alert.startedAt()));
        }
        // Video and audio recorded during the alert: the contact can watch them to help find her
        List<PublicEvidence> recordings = evidences.ofAlert(alert.id()).stream()
                .map(e -> new PublicEvidence(e.id(), e.mediaType(), e.contentType(), e.createdAt()))
                .toList();
        return new TrackingView(name, alert.status(), "active".equals(alert.status()), alert.startedAt(),
                end(alert), route, settings.emergencyNumber(), recordings, alert.activationMethod(),
                AlertNoticesService.serverReason(alert));
    }

    @Override
    @Transactional(readOnly = true)
    public EvidenceFile evidence(String code, Long evidenceId, LocalDateTime now) {
        AlertSummary alert = liveAlert(code, now);
        return evidences.file(alert.id(), evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("La evidencia no existe"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertMessageView> messages(String code, Long afterId, LocalDateTime now) {
        return alerts.messages(liveAlert(code, now).id(), afterId);
    }

    @Override
    @Transactional
    public AlertMessageView sendMessage(String code, String name, String text, LocalDateTime now) {
        AlertSummary alert = liveAlert(code, now);
        limit.registrar(TrackingLink.hash(code), now);
        return alerts.contactMessage(alert.id(), name, text);
    }

    private AlertSummary liveAlert(String code, LocalDateTime now) {
        if (!TrackingLink.possibleFormat(code)) throw unavailable();
        TrackingLink link = links.byHash(TrackingLink.hash(code)).orElseThrow(PublicTrackingService::unavailable);
        if (link.revoked()) throw unavailable();
        AlertSummary alert = alerts.find(link.getAlertId()).orElseThrow(PublicTrackingService::unavailable);
        if (!TrackingLink.liveTracking(alert.status(), end(alert), now)) throw unavailable();
        return alert;
    }

    static LocalDateTime end(AlertSummary a) {
        return a.endedAt() != null ? a.endedAt() : a.cancelledAt();
    }

    private static ResourceNotFoundException unavailable() {
        return new ResourceNotFoundException("El enlace de seguimiento no existe o ya venció");
    }
}
