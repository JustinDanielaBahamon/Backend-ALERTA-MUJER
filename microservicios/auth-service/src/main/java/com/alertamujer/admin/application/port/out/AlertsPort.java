package com.alertamujer.admin.application.port.out;

import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.alerts.api.AlertsApi.ExpirationStatus;

import java.util.List;
import java.util.Map;

/** Output port to Alert Service (through /internal/rpc). */
public interface AlertsPort {

    /** Number of alerts per user_profile.id. */
    Map<Long, Long> countByProfile();

    /** All alerts with their user, newest to oldest. */
    List<AdminAlertView> all();

    long active();

    ExpirationStatus lastExpiration();
}
