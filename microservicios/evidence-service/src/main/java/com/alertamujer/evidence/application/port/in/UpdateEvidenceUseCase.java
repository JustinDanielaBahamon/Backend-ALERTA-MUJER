package com.alertamujer.evidence.application.port.in;

import com.alertamujer.evidence.domain.model.Evidence;

/** Use cases for the user's changes (and deletion by the administration). */
public interface UpdateEvidenceUseCase {

    EvidenceWithAlert rename(Long id, String name, Long userId);

    /** If admin is true any evidence is deleted; otherwise only one belonging to the user. */
    void delete(Long id, Long userId, boolean admin);

    /** Record without a file (old clients that only send a URL). */
    Evidence registerWithoutFile(Long alertId, String mediaType, String fileUrl, Long userId);

    Evidence updateWithoutFile(Long id, String mediaType, String fileUrl, Long userId);
}
