package com.alertamujer.notifications.application.port.out;

import java.time.LocalDateTime;
import java.util.List;

/** Output port: active alerts whose tracking nobody opened, and the mark that they were escalated. */
public interface EscalationRepository {

    /** Active, not escalated, started between both moments and without any tracking visit. */
    List<UnseenAlert> unseen(LocalDateTime startedAfter, LocalDateTime startedBefore);

    /** Marks the alert as escalated; false if another instance already did it. */
    boolean claim(Long alertId, LocalDateTime now);

    record UnseenAlert(Long alertId, Long userProfileId, LocalDateTime startedAt) {}
}
