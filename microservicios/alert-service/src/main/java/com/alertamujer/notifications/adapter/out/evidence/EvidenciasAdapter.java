package com.alertamujer.notifications.adapter.out.evidence;

import com.alertamujer.evidence.api.EvidenceApi;
import com.alertamujer.evidence.api.EvidenceApi.EvidenceFile;
import com.alertamujer.evidence.api.EvidenceApi.EvidenceSummary;
import com.alertamujer.notifications.application.port.out.EvidenciasPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adaptador de salida hacia Evidence Service (EvidenceApiHttpClient en este microservicio). */
@Component
class EvidenciasAdapter implements EvidenciasPort {

    private final EvidenceApi evidenceApi;

    EvidenciasAdapter(EvidenceApi evidenceApi) {
        this.evidenceApi = evidenceApi;
    }

    @Override
    public List<EvidenceSummary> deAlerta(Long alertId) {
        return evidenceApi.evidenciasDeAlerta(alertId);
    }

    @Override
    public Optional<EvidenceFile> archivo(Long alertId, Long evidenciaId) {
        return evidenceApi.archivo(alertId, evidenciaId);
    }
}
