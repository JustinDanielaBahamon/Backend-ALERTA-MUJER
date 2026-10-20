package com.alertamujer.identity.api;

/**
 * Se eliminó una cuenta (la propia usuaria o con la aprobación de dos administradoras). Los demás
 * servicios borran los datos personales que guardan de ella. userProfileId puede ser null si no
 * tenía perfil.
 */
public record AccountDeletedEvent(Long userId, Long userProfileId) {}
