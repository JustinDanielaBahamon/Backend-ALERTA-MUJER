package com.alertamujer.identity.domain.model;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Cambios de la usuaria en sus datos. Un campo null no se envió y se deja como estaba; un Optional
 * vacío se envió vacío y borra el dato. birthdate null = sin cambio (una fecha inválida se ignora).
 */
public record UserChanges(Optional<String> firstName, Optional<String> lastName, Optional<String> telephone,
                             Optional<String> documentNumber, Optional<String> documentType, LocalDate birthdate,
                             Optional<String> profilePhoto) {
}
