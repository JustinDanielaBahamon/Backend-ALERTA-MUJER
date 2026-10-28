package com.alertamujer.notifications.application.port.out;

import com.alertamujer.evidence.api.EvidenceApi.EvidenceFile;
import com.alertamujer.evidence.api.EvidenceApi.EvidenceSummary;

import java.util.List;
import java.util.Optional;

/** Output port to Evidence Service: alert recordings for the tracking page. */
public interface EvidencePort {

    List<EvidenceSummary> ofAlert(Long alertId);

    /** Only if the evidence belongs to that alert. */
    Optional<EvidenceFile> file(Long alertId, Long evidenceId);
}
