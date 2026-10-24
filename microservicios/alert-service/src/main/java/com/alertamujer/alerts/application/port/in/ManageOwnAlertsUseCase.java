package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.AlertChanges;

import java.util.List;
import java.util.Optional;

/** The user's use cases for her alerts. */
public interface ManageOwnAlertsUseCase {

    /** Newest to oldest. */
    List<Alert> mine(Long userId);

    /** Empty if it does not exist or belongs to another user. */
    Optional<Alert> one(Long id, Long userId);

    /**
     * A user's alerts. Clients send the user id (users.id); the own profile id is also
     * accepted. Only the administration sees another user's.
     */
    List<Alert> ofUser(Long requestedId, Long userId);

    /**
     * Activates an alert and records who gets notified. A user can only have one ongoing: if
     * one is already active it is returned, without creating another or notifying the contacts again.
     */
    Alert activate(NewAlert newOne, Long userId);

    Alert update(Long id, AlertChanges changes, Long userId);

    /** Keep (or not) the alert's evidence beyond the retention days. */
    Alert keepEvidence(Long id, boolean keep, Long userId);

    void delete(Long id, Long userId);
}
