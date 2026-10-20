package com.alertamujer.evidence.application.port.out;

import com.alertamujer.identity.api.UserSummary;

import java.util.Map;
import java.util.Optional;

/**
 * Puerto de salida: datos de las usuarias. Hoy lo resuelve el módulo identity en memoria; al separar
 * microservicios, un adaptador HTTP hacia identity-service.
 */
public interface IdentidadPort {

    /** El JWT trae users.id; las alertas pertenecen a user_profile.id. */
    Long perfilDe(Long userId);

    Map<Long, UserSummary> usuariasPorPerfil();

    Optional<UserSummary> usuariaDelPerfil(Long userProfileId);
}
