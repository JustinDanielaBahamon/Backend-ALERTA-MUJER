package com.alertamujer.identity.api;

import java.util.Map;
import java.util.Optional;

/**
 * Consultas del módulo identity para los demás módulos. Los ids de entrada son siempre users.id,
 * salvo que el nombre del método diga lo contrario (perfil = user_profile.id, cuenta = account.id).
 */
public interface IdentityApi {

    /** Perfil de la usuaria en la app; lanza ResourceNotFoundException si no tiene. */
    Long requireProfileId(Long userId);

    Optional<Long> findProfileId(Long userId);

    /** Cuenta (credenciales) de la persona; lanza ResourceNotFoundException si no tiene. */
    Long requireAccountId(Long userId);

    Optional<UserSummary> findUser(Long userId);

    Optional<UserSummary> findUserByEmail(String email);

    Optional<UserSummary> findUserByProfileId(Long profileId);

    Optional<UserSummary> findUserByAccountId(Long accountId);

    /** Todas las personas, por users.id (listados del panel). */
    Map<Long, UserSummary> usersById();

    /** Todas las personas con perfil de app, por user_profile.id. */
    Map<Long, UserSummary> usersByProfileId();

    /** Todas las personas con cuenta, por account.id. */
    Map<Long, UserSummary> usersByAccountId();
}
