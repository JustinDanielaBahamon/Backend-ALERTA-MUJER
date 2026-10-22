package com.alertamujer.identity.domain.model;

import java.time.LocalDate;
import java.util.Optional;

/**
 * The user's changes to her data. A null field was not sent and is left as it was; an empty
 * Optional was sent empty and erases the value. birthdate null = no change (an invalid date is ignored).
 */
public record UserChanges(Optional<String> firstName, Optional<String> lastName, Optional<String> telephone,
                             Optional<String> documentNumber, Optional<String> documentType, LocalDate birthdate,
                             Optional<String> profilePhoto) {
}
