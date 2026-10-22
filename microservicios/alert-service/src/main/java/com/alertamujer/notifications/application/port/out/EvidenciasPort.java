package com.alertamujer.notifications.application.port.out;

import com.alertamujer.evidence.api.EvidenceApi.EvidenceFile;
import com.alertamujer.evidence.api.EvidenceApi.EvidenceSummary;

import java.util.List;
import java.util.Optional;

/** Puerto de salida hacia Evidence Service: grabaciones de la alerta para el seguimiento. */
public interface EvidenciasPort {

    List<EvidenceSummary> deAlerta(Long alertId);

    /** Solo si la evidencia pertenece a esa alerta. */
    Optional<EvidenceFile> file(Long alertId, Long evidenciaId);
}
