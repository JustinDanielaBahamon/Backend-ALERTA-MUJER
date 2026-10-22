package com.alertamujer.evidence.application.port.in;

import com.alertamujer.evidence.domain.model.Evidence;
import org.springframework.core.io.Resource;

import java.util.List;

/** User read use cases: her evidence, one evidence and its file. */
public interface QueryEvidenceUseCase {

    List<EvidenceWithAlert> ofUser(Long userId);

    Evidence una(Long id, Long userId);

    /** Evidence file; if admin is true the owner is not checked. */
    EvidenceWithContent file(Long id, Long userId, boolean admin);

    record EvidenceWithContent(Evidence evidence, Resource content) {}
}
