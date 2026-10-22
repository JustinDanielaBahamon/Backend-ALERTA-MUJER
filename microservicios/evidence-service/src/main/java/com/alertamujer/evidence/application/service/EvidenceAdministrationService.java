package com.alertamujer.evidence.application.service;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.evidence.application.port.in.ManageEvidenceUseCase;
import com.alertamujer.evidence.application.port.out.AlertsPort;
import com.alertamujer.evidence.application.port.out.EvidenceRepository;
import com.alertamujer.evidence.application.port.out.IdentityPort;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Admin panel use cases: evidence with its alert and owner, stats and review. */
@Service
public class EvidenceAdministrationService implements ManageEvidenceUseCase {

    private final EvidenceRepository repository;
    private final AlertsPort alerts;
    private final IdentityPort identidad;

    public EvidenceAdministrationService(EvidenceRepository repository, AlertsPort alerts, IdentityPort identidad) {
        this.repository = repository;
        this.alerts = alerts;
        this.identidad = identidad;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminEvidence> listAll() {
        Map<Long, AlertSummary> byAlert = alerts.todas();
        Map<Long, UserSummary> byProfile = identidad.usersByProfile();
        return repository.todas().stream()
                .sorted(Comparator.comparing(Evidence::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(e -> {
                    AlertSummary a = byAlert.get(e.getAlertId());
                    return new AdminEvidence(e, a, a == null ? null : byProfile.get(a.userProfileId()));
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Stats stats() {
        List<Evidence> todas = repository.todas();
        long verificadas = todas.stream().filter(e -> "verified".equals(e.getReviewStatus())).count();
        long rechazadas = todas.stream().filter(e -> "rejected".equals(e.getReviewStatus())).count();
        long tamanio = todas.stream().mapToLong(e -> e.getSizeBytes() == null ? 0 : e.getSizeBytes()).sum();
        return new Stats(todas.size(), verificadas, todas.size() - verificadas - rechazadas, rechazadas, tamanio);
    }

    @Override
    @Transactional
    public AdminEvidence changeReview(Long id, String review) {
        Evidence e = repository.find(id).orElseThrow(() -> new ResourceNotFoundException("Evidence", id));
        e.changeReview(review);
        e = repository.save(e);
        AlertSummary a = alerts.find(e.getAlertId()).orElse(null);
        UserSummary u = a == null ? null : identidad.userOfProfile(a.userProfileId()).orElse(null);
        return new AdminEvidence(e, a, u);
    }
}
