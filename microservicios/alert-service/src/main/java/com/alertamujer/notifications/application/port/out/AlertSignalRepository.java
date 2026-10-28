package com.alertamujer.notifications.application.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Output port: the last signal of the active alerts (alert.alert last_signal_at / signal_lost_at). */
public interface AlertSignalRepository {

    /**
     * Active alerts activated by her (not the ones the server activated in a trip), started after
     * startedAfter, whose last signal is older than silentSince and that were not warned about since then.
     */
    List<SilentAlert> silent(LocalDateTime silentSince, LocalDateTime startedAfter);

    /** Marks that the contacts are being told; false if another instance already did it. */
    boolean claim(Long alertId, LocalDateTime now);

    /** The warning the contacts see on the tracking page now (AlertWarningEvent kinds), if any. */
    Optional<String> currentWarning(Long alertId);

    record SilentAlert(Long alertId, Long userProfileId, LocalDateTime lastSignal) {}
}
