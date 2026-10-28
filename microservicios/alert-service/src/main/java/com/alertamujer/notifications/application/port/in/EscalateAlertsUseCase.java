package com.alertamujer.notifications.application.port.in;

import java.time.LocalDateTime;

/**
 * Escalation: if a few minutes after an alert started no contact has opened its tracking, the notice is
 * sent again (email and push), only once per alert.
 */
public interface EscalateAlertsUseCase {

    /** Returns how many alerts were escalated. */
    int escalate(LocalDateTime now);
}
