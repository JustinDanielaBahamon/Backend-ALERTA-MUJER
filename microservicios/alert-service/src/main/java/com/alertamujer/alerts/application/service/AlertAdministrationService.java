package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.alerts.application.port.in.ManageAlertsUseCase;
import com.alertamujer.alerts.application.port.in.AlertRecipientsUseCase.NotifiedContact;
import com.alertamujer.alerts.application.port.in.AlertRecipientsUseCase;
import com.alertamujer.alerts.application.port.out.AlertRepository;
import com.alertamujer.alerts.application.port.out.AlertEventsPort;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.application.port.out.LocationRepository;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.LocationLog;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Alertas para el panel: los campos de la alerta más la usuaria que la activó (nombre y correo). */
@Service
public class AlertAdministrationService implements ManageAlertsUseCase {

    private final AlertRepository repository;
    private final IdentityPort identity;
    private final AlertEventsPort events;
    private final LocationRepository locations;
    private final AlertRecipientsUseCase recipients;

    public AlertAdministrationService(AlertRepository repository, IdentityPort identity, AlertEventsPort events,
                                        LocationRepository locations, AlertRecipientsUseCase recipients) {
        this.repository = repository;
        this.identity = identity;
        this.events = events;
        this.locations = locations;
        this.recipients = recipients;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminAlertView> listAll() {
        Map<Long, UserSummary> byProfile = identity.usersByProfile();
        return repository.all().stream()
                .sorted(Comparator.comparing(Alert::getStartedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(a -> {
                    UserSummary u = byProfile.get(a.getUserProfileId());
                    return new AdminAlertView(a.getId(), a.getUserProfileId(), u != null ? u.id() : null,
                            u != null ? u.fullName() : null, u != null ? u.email() : null,
                            a.getDeviceId(), a.getAlertType(), a.getActivationMethod(), a.getStatus(),
                            a.getMessage(), a.getLatitude(), a.getLongitude(),
                            a.getStartedAt(), a.getEndedAt(), a.getCancelledAt(), a.getCreatedAt());
                })
                .toList();
    }

    @Override
    @Transactional
    public Alert changeStatus(Long id, String status) {
        Alert alert = repository.find(id).orElseThrow(() -> new ResourceNotFoundException("Alert", id));
        LocalDateTime now = LocalDateTime.now();
        if (alert.changeStatus(status, now)) events.alertEnded(alert, now);
        return repository.save(alert);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDetail detail(Long id) {
        Alert alert = repository.find(id).orElseThrow(() -> new ResourceNotFoundException("Alert", id));
        boolean visible = alert.isActive() || "resolved".equals(alert.getStatus());
        List<LocationLog> points = visible ? locations.ofAlert(id) : List.of();
        LastLocation latest = points.stream()
                .max(Comparator.comparing(LocationLog::getRecordedAt))
                .map(l -> new LastLocation(l.getLatitude(), l.getLongitude(), l.getAccuracy(), l.getRecordedAt()))
                .orElse(null);
        List<NotifiedContact> contacts = recipients.contactsOf(alert);
        List<AdminDelivery> deliveries = new java.util.ArrayList<>();
        for (int i = 0; i < contacts.size(); i++) {
            NotifiedContact c = contacts.get(i);
            deliveries.add(new AdminDelivery(i + 1, c.relationship(), c.channels().stream()
                    .map(k -> new AdminChannel(k.channel(), k.status(), k.sentAt(), k.deliveredAt())).toList()));
        }
        return new AdminDetail(visible, points.size(), latest, deliveries);
    }
}
