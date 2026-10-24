package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.Alert;

/** While her alert goes on, the user says she is still in danger, or "cancels" it under duress. */
public interface AlertCheckInUseCase {

    /** "Sigo en peligro": the alert is extended (never beyond the absolute limit). */
    Alert extend(Long alertId, Long userId);

    /**
     * Duress PIN: on her phone it looks cancelled, but the alert goes on and the contacts are told
     * that she may be under pressure.
     */
    Alert duress(Long alertId, Long userId);
}
