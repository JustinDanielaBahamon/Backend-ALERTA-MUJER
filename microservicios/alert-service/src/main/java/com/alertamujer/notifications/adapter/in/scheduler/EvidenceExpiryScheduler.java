package com.alertamujer.notifications.adapter.in.scheduler;

import com.alertamujer.notifications.application.port.in.NoticeEvidenceExpiryUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Input adapter: every day at 10 a. m. (Colombia) the evidence about to be deleted is announced. */
@Component
class EvidenceExpiryScheduler {

    private final NoticeEvidenceExpiryUseCase notice;

    EvidenceExpiryScheduler(NoticeEvidenceExpiryUseCase notice) {
        this.notice = notice;
    }

    @Scheduled(cron = "${evidence.expiry-notice.cron:0 0 10 * * *}", zone = "America/Bogota")
    void notice() {
        notice.notice(LocalDateTime.now());
    }
}
