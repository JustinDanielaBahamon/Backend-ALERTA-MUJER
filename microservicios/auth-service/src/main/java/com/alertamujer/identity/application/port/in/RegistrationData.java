package com.alertamujer.identity.application.port.in;

/**
 * Formulario de registro. fechaNacimiento en DD/MM/AAAA, como lo escriben los formularios.
 * aceptaTerminos: true si marcó la casilla; null si la app es anterior y no lo envía.
 */
public record RegistrationData(String name, String email, String password, String telephone,
                            String documentType, String documentNumber, String birthDate,
                            Boolean acceptsTerms) {
}
