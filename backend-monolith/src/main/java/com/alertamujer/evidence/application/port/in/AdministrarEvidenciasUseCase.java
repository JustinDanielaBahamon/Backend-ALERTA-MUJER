package com.alertamujer.evidence.application.port.in;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.identity.api.UserSummary;

import java.util.List;

/** Casos de uso del panel de administración. */
public interface AdministrarEvidenciasUseCase {

    List<EvidenciaAdmin> listar();

    Estadisticas estadisticas();

    EvidenciaAdmin cambiarRevision(Long id, String revision);

    /** Evidencia con su alerta y su dueña (cualquiera de las dos puede faltar). */
    record EvidenciaAdmin(Evidence evidencia, AlertSummary alerta, UserSummary duena) {}

    record Estadisticas(long total, long verified, long pending, long rejected, long totalSizeBytes) {}
}
