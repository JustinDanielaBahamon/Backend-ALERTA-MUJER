package com.alertamujer.identity.application.port.out;

/** Output port: issuing and reading the session JWTs. */
public interface TokensPort {

    /** Access token (24 hours) with the person's role. */
    String access(Long userId, String role);

    /** Refresh token (15 days) to refresh the session without asking for the password. */
    String refresh(Long userId);

    /**
     * users.id of a valid refresh token.
     * @throws RuntimeException if the signature is invalid, it expired or it is not a refresh token
     */
    Long userForRefresh(String token);
}
