package com.alertamujer.identity.application.port.in;

/** Registration with email and password. */
public interface RegisterUserUseCase {

    /**
     * Creates the person, her account and her profile and opens the session. If the email, phone or
     * document is already used by another account, it answers with all the repeated fields at once.
     */
    Session registrar(RegistrationData data);
}
