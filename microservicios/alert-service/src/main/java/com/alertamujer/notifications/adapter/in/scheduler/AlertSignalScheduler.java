package com.alertamujer.notifications.adapter.in.scheduler;

import com.alertamujer.notifications.application.port.in.WatchAlertSignalUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Input adapter: every minute looks for active alerts whose phone stopped sending its location. */
@Component
class AlertSignalScheduler {

    private final WatchAlertSignalUseCase watch;

    AlertSignalScheduler(WatchAlertSignalUseCase watch) {
        this.watch = watch;
    }

    @Scheduled(fixedDelayString = "${alerts.signal-check-ms:60000}", initialDelay = 40000)
    void review() {
        watch.review(LocalDateTime.now());
    }
}
