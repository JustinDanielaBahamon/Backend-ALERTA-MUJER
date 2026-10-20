package com.alertamujer.evidence.application.port.in;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.evidence.domain.model.Evidence;

/** Evidencia con el resumen de su alerta (para listarla sin pedir cada alerta aparte). */
public record EvidenciaConAlerta(Evidence evidencia, AlertSummary alerta) {}
