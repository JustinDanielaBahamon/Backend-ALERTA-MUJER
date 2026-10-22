package com.alertamujer.admin.application.port.out;

import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.alerts.api.AlertsApi.ExpirationStatus;

import java.util.List;
import java.util.Map;

/** Puerto de salida hacia Alert Service (por /internal/rpc). */
public interface AlertsPort {

    /** Número de alertas por user_profile.id. */
    Map<Long, Long> countByProfile();

    /** Todas las alertas con su usuaria, de la más reciente a la más antigua. */
    List<AdminAlertView> all();

    long active();

    ExpirationStatus lastExpiration();
}
