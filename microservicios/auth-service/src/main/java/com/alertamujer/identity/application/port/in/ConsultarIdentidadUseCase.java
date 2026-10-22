package com.alertamujer.identity.application.port.in;

import com.alertamujer.identity.domain.model.User;

import java.util.Map;
import java.util.Optional;

/** Consultas que piden los otros módulos y microservicios. */
public interface ConsultarIdentidadUseCase {

    Optional<Long> profileOf(Long userId);

    Optional<Long> cuentaDe(Long userId);

    Optional<User> usuaria(Long userId);

    Optional<User> porCorreo(String correo);

    Optional<User> byProfile(Long perfilId);

    Optional<User> porCuenta(Long accountId);

    Map<Long, User> porId();

    Map<Long, User> porIdDePerfil();

    Map<Long, User> porIdDeCuenta();
}
