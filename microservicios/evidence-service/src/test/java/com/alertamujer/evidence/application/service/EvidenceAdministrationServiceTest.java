package com.alertamujer.evidence.application.service;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.evidence.application.port.in.ManageEvidenceUseCase.Stats;
import com.alertamujer.evidence.application.port.in.ManageEvidenceUseCase.AdminEvidence;
import com.alertamujer.evidence.application.port.out.AlertsPort;
import com.alertamujer.evidence.application.port.out.EvidenceRepository;
import com.alertamujer.evidence.application.port.out.IdentityPort;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.identity.api.UserSummary;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EvidenceAdministrationServiceTest {

    private final EvidenceRepository repository = mock(EvidenceRepository.class);
    private final AlertsPort alerts = mock(AlertsPort.class);
    private final IdentityPort identity = mock(IdentityPort.class);
    private final EvidenceAdministrationService service = new EvidenceAdministrationService(repository, alerts, identity);

    private Evidence evidence(Long id, String review, long size) {
        Evidence e = new Evidence();
        e.setId(id);
        e.setAlertId(5L);
        e.setSizeBytes(size);
        e.setStorageKey("clave-" + id);
        e.setReviewStatus(review);
        return e;
    }

    @Test
    void listAllAddsUserAndAlert() {
        AlertSummary alert = new AlertSummary(5L, 10L, "main", "panic_button", "active", null, null, null, null, null, null);
        UserSummary u = new UserSummary(1L, "Ana", "Gómez", "ana@correo.com", null, 1L, null);
        when(repository.all()).thenReturn(List.of(evidence(1L, "pending", 100)));
        when(alerts.all()).thenReturn(Map.of(5L, alert));
        when(identity.usersByProfile()).thenReturn(Map.of(10L, u));

        AdminEvidence view = service.listAll().get(0);

        assertEquals("Ana Gómez", view.owner().fullName());
        assertEquals("active", view.alert().status());
    }

    @Test
    void statsCountsByReviewAndSumsSizes() {
        when(repository.all()).thenReturn(List.of(
                evidence(1L, "verified", 100), evidence(2L, "rejected", 50), evidence(3L, "pending", 25)));

        Stats stats = service.stats();

        assertEquals(3, stats.total());
        assertEquals(1, stats.verified());
        assertEquals(1, stats.pending());
        assertEquals(1, stats.rejected());
        assertEquals(175, stats.totalSizeBytes());
    }

    @Test
    void changeReviewSavesTheStatus() {
        when(repository.find(1L)).thenReturn(Optional.of(evidence(1L, "pending", 100)));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(alerts.find(5L)).thenReturn(Optional.empty());

        assertEquals("verified", service.changeReview(1L, "verified").evidence().getReviewStatus());
    }
}
