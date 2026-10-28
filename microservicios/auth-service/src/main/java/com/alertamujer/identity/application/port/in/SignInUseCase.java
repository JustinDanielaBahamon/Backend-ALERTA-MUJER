package com.alertamujer.identity.application.port.in;

/** Sign in, refresh and sign out. */
public interface SignInUseCase {

    Session withEmail(String email, String password);

    /**
     * With Google (Firebase token on the web or Google token on mobile). If the email is already
     * registered it signs in to that account; otherwise it creates the user (who can only sign in with
     * Google).
     */
    Session withGoogle(String idToken);

    /** New access token from the refresh one. */
    String refresh(String refreshToken);

    /** Voluntary or inactivity sign-out (the JWT is not revoked; it is audited if she is an admin). */
    void close(Long userId, String reason);
}
