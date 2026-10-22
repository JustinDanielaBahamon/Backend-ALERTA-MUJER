package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.QueryAlertsUseCase;
import com.alertamujer.alerts.application.port.in.ManageOwnAlertsUseCase;
import com.alertamujer.alerts.application.port.in.NewAlert;
import com.alertamujer.alerts.application.port.out.AlertRepository;
import com.alertamujer.alerts.application.port.out.AlertEventsPort;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.AlertChanges;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Casos de uso de las alertas. Solo conoce puertos, no la base ni los otros microservicios. */
@Service
public class AlertsService implements ManageOwnAlertsUseCase, QueryAlertsUseCase {

    private final AlertRepository repository;
    private final RecipientsService recipients;
    private final IdentityPort identity;
    private final AlertEventsPort events;

    public AlertsService(AlertRepository repository, RecipientsService recipients,
                          IdentityPort identity, AlertEventsPort events) {
        this.repository = repository;
        this.recipients = recipients;
        this.identity = identity;
        this.events = events;
    }

    // ── De la usuaria ───────────────────────────────────────────────

    @Override
    public List<Alert> mias(Long userId) {
        return repository.ofUser(profileOf(userId)).stream()
                .sorted(Comparator.comparing(Alert::getStartedAt).reversed())
                .toList();
    }

    @Override
    public Optional<Alert> one(Long id, Long userId) {
        Long profile = identity.profileOf(userId).orElse(null);
        if (profile == null) return Optional.empty();
        return repository.find(id).filter(a -> a.esDe(profile));
    }

    @Override
    public List<Alert> ofUser(Long requestedId, Long userId) {
        Long own = profileOf(userId);
        UserSummary current = identity.user(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
        // users.id y user_profile.id no coinciden necesariamente (en producción son distintos)
        if (requestedId.equals(userId) || requestedId.equals(own)) {
            return repository.ofUser(own);
        }
        if (!current.admin()) {
            throw new AccessDeniedException("No puedes ver alertas de otro usuario");
        }
        // Para la administración el id se interpreta como users.id y, si no existe, como user_profile.id
        return repository.ofUser(identity.profileOf(requestedId).orElse(requestedId));
    }

    @Override
    @Transactional
    public Alert activate(NewAlert newOne, Long userId) {
        // La dueña sale del JWT; nunca se confía en un perfil que llegue en el body (evita IDOR)
        Long profile = profileOf(userId);
        Optional<Alert> ongoing = repository.ongoing(profile);
        if (ongoing.isPresent()) return ongoing.get();

        Alert alert = new Alert();
        alert.setUserProfileId(profile);
        alert.setDeviceId(newOne.deviceId());
        if (newOne.alertType() != null) alert.setAlertType(newOne.alertType());
        alert.setActivationMethod(newOne.activationMethod());
        if (newOne.status() != null) alert.setStatus(newOne.status());
        alert.setMessage(newOne.message());
        alert.setLatitude(newOne.latitude());
        alert.setLongitude(newOne.longitude());
        if (newOne.startedAt() != null) alert.setStartedAt(newOne.startedAt());

        Alert created = repository.save(alert);
        recipients.registrar(created, newOne.contactIds());
        return created;
    }

    @Override
    @Transactional
    public Alert update(Long id, AlertChanges changes, Long userId) {
        Alert alert = own(id, userId, "No puedes modificar alertas de otro usuario");
        LocalDateTime now = LocalDateTime.now();
        // Al terminar una alerta activa se avisa a los contactos que la recibieron
        if (alert.update(changes, now)) events.alertEnded(alert, now);
        return repository.save(alert);
    }

    @Override
    @Transactional
    public Alert keepEvidence(Long id, boolean keep, Long userId) {
        Alert alert = own(id, userId, "No puedes modificar alertas de otro usuario");
        alert.keepEvidence(keep, repository.kept(alert.getUserProfileId()));
        return repository.save(alert);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        own(id, userId, "No puedes eliminar alertas de otro usuario");
        repository.delete(id);
    }

    // ── Para otros módulos ──────────────────────────────────────────

    @Override
    public Optional<Alert> find(Long id) {
        return id == null ? Optional.empty() : repository.find(id);
    }

    @Override
    public List<Alert> findMany(Collection<Long> ids) {
        return ids.isEmpty() ? List.of() : repository.findMany(ids);
    }

    @Override
    public List<Alert> all() {
        return repository.all();
    }

    @Override
    public Map<Long, Long> countByProfile() {
        return repository.all().stream()
                .filter(a -> a.getUserProfileId() != null)
                .collect(Collectors.groupingBy(Alert::getUserProfileId, Collectors.counting()));
    }

    @Override
    public long activeCount() {
        return repository.active().size();
    }

    @Override
    public List<Long> profileIds(Long profile) {
        return repository.ofUser(profile).stream().map(Alert::getId).toList();
    }

    @Override
    public Set<Long> keepEvidence(Collection<Long> ids) {
        if (ids.isEmpty()) return Set.of();
        return repository.findMany(ids).stream()
                .filter(Alert::isKeepEvidence)
                .map(Alert::getId)
                .collect(Collectors.toSet());
    }

    // ── Apoyo ───────────────────────────────────────────────────────

    private Long profileOf(Long userId) {
        return identity.profileOf(userId).orElseThrow(() -> new ResourceNotFoundException("UserProfile for user", userId));
    }

    private Alert own(Long id, Long userId, String messageOfForeignAlert) {
        Alert alert = repository.find(id).orElseThrow(() -> new ResourceNotFoundException("Alert", id));
        if (!alert.esDe(profileOf(userId))) throw new AccessDeniedException(messageOfForeignAlert);
        return alert;
    }
}
