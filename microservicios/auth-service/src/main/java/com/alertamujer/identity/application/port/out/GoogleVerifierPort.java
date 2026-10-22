package com.alertamujer.identity.application.port.out;

/** Puerto de salida: valida el token de Google o Firebase y devuelve la identidad verificada. */
public interface GoogleVerifierPort {

    /** @throws com.alertamujer.shared.exception.InvalidCredentialsException si el token no es válido */
    GoogleIdentity verify(String idToken);

    record GoogleIdentity(String email, String name, String givenName, String familyName) {}
}
