package com.alertamujer.notifications.application.port.out;

import java.time.LocalDateTime;
import java.util.List;

/** Output port: whose evidence (of alerts not kept) was recorded within a period. */
public interface EvidenceExpiryRepository {

    /** One row per user (users.id) with the number of her alerts that have evidence in [from, to). */
    List<UserWithExpiring> recordedBetween(LocalDateTime from, LocalDateTime to);

    record UserWithExpiring(Long userId, int alerts) {}
}
