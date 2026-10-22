package com.alertamujer.identity.application.port.out;

/** Output port: validates the Google or Firebase token and returns the verified identity. */
public interface GoogleVerifierPort {

    /** @throws com.alertamujer.shared.exception.InvalidCredentialsException if the token is not valid */
    GoogleIdentity verify(String idToken);

    record GoogleIdentity(String email, String name, String givenName, String familyName) {}
}
