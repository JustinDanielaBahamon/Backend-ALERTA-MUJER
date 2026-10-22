package com.alertamujer.evidence.application.port.in;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.evidence.domain.model.Evidence;

/** Evidence with its alert summary (to list it without fetching each alert separately). */
public record EvidenceWithAlert(Evidence evidence, AlertSummary alert) {}
