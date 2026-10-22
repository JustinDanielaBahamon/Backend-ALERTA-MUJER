package com.alertamujer.evidence.application.port.in;

import java.time.LocalDateTime;

/**
 * Use case: delete expired evidence (60 days) except for kept alerts, and all evidence of a user who
 * deleted her account.
 */
public interface CleanUpEvidenceUseCase {

    /** Returns how many were deleted. */
    int clear(LocalDateTime ahora);

    /** Deleted account: removes all of her evidence, even what she had marked to keep. */
    int removeForUser(Long userProfileId);
}
