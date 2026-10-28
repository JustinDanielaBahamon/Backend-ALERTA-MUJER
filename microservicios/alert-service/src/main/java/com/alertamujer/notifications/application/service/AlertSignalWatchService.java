package com.alertamujer.notifications.application.service;

import com.alertamujer.alerts.api.AlertWarningEvent;
import com.alertamujer.notifications.application.port.in.NotifyContactsUseCase;
import com.alertamujer.notifications.application.port.in.WatchAlertSignalUseCase;
import com.alertamujer.notifications.application.port.out.AlertSignalRepository;
import com.alertamujer.notifications.application.port.out.AlertSignalRepository.SilentAlert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Tells the contacts when the phone of an active alert stops sending its location (5 minutes by
 * default; the app sends it every 15-60 s, also without GPS with the last known one). Once per loss:
 * if the signal comes back and is lost again, they are told again.
 */
@Service
public class AlertSignalWatchService implements WatchAlertSignalUseCase {

    private static final Logger log = LoggerFactory.getLogger(AlertSignalWatchService.class);
    /** Alerts older than this are already closed (absolute limit) or about to be. */
    static final Duration MAX_AGE = Duration.ofHours(3);

    private final AlertSignalRepository signals;
    private final NotifyContactsUseCase notices;
    private final Duration silence;

    public AlertSignalWatchService(AlertSignalRepository signals, NotifyContactsUseCase notices,
                                   @Value("${alerts.signal-lost-minutes:5}") long silenceMinutes) {
        this.signals = signals;
        this.notices = notices;
        this.silence = Duration.ofMinutes(silenceMinutes);
    }

    @Override
    public int review(LocalDateTime now) {
        int warned = 0;
        for (SilentAlert alert : signals.silent(now.minus(silence), now.minus(MAX_AGE))) {
            if (!signals.claim(alert.alertId(), now)) continue;
            try {
                notices.alertWarning(alert.alertId(), alert.userProfileId(), AlertWarningEvent.SIGNAL_LOST, alert.lastSignal());
                warned++;
            } catch (RuntimeException e) {
                log.error("No se pudo avisar la pérdida de señal de la alerta {}: {}", alert.alertId(), e.getMessage());
            }
        }
        return warned;
    }
}
