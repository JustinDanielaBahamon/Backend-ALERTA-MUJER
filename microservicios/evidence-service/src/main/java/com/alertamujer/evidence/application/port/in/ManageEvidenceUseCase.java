package com.alertamujer.evidence.application.port.in;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.identity.api.UserSummary;

import java.util.List;

/** Admin panel use cases. */
public interface ManageEvidenceUseCase {

    List<AdminEvidence> listAll();

    Stats stats();

    AdminEvidence changeReview(Long id, String review);

    /** Evidence with its alert and its owner (either one may be missing). */
    record AdminEvidence(Evidence evidence, AlertSummary alert, UserSummary owner) {}

    record Stats(long total, long verified, long pending, long rejected, long totalSizeBytes) {}
}
