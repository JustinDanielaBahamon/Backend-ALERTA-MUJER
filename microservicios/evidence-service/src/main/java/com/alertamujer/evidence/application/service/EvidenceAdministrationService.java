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
    private final IdentityPort identity;

    public EvidenceAdministrationService(EvidenceRepository repository, AlertsPort alerts, IdentityPort identity) {
        this.repository = repository;
        this.alerts = alerts;
        this.identity = identity;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminEvidence> listAll() {
        Map<Long, AlertSummary> byAlert = alerts.all();
        Map<Long, UserSummary> byProfile = identity.usersByProfile();
        return repository.all().stream()
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
        List<Evidence> all = repository.all();
        long verificadas = all.stream().filter(e -> "verified".equals(e.getReviewStatus())).count();
        long rechazadas = all.stream().filter(e -> "rejected".equals(e.getReviewStatus())).count();
        long tamanio = all.stream().mapToLong(e -> e.getSizeBytes() == null ? 0 : e.getSizeBytes()).sum();
        return new Stats(all.size(), verificadas, all.size() - verificadas - rechazadas, rechazadas, tamanio);
    }

    @Override
    @Transactional
    public AdminEvidence changeReview(Long id, String review) {
        Evidence e = repository.find(id).orElseThrow(() -> new ResourceNotFoundException("Evidence", id));
        e.changeReview(review);
        e = repository.save(e);
        AlertSummary a = alerts.find(e.getAlertId()).orElse(null);
        UserSummary u = a == null ? null : identity.userOfProfile(a.userProfileId()).orElse(null);
        return new AdminEvidence(e, a, u);
    }
}
