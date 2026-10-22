package com.alertamujer.identity.application.port.in;

/**
 * Registration form. fechaNacimiento in DD/MM/YYYY, as the forms write it.
 * aceptaTerminos: true if she checked the box; null if the app is older and does not send it.
 */
public record RegistrationData(String name, String email, String password, String telephone,
                            String documentType, String documentNumber, String birthDate,
                            Boolean acceptsTerms) {
}
