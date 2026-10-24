package com.alertamujer.notifications.application.port.in;

import java.time.LocalDateTime;

/**
 * A few days before her evidence is deleted, each user gets one notice on her phone (not one per
 * alert), so she can keep it or download it.
 */
public interface NoticeEvidenceExpiryUseCase {

    /** Returns how many users were told. */
    int notice(LocalDateTime now);
}
