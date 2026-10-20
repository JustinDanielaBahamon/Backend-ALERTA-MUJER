package com.alertamujer.identity.application.port.out;

/** Puerto de salida: valida el token de Google o Firebase y devuelve la identidad verificada. */
public interface VerificadorGooglePort {

    /** @throws com.alertamujer.shared.exception.InvalidCredentialsException si el token no es válido */
    IdentidadGoogle verificar(String idToken);

    record IdentidadGoogle(String email, String name, String givenName, String familyName) {}
}
