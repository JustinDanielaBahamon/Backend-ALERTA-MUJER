package com.alertamujer.identity.application.port.out;

/** Output port: notices to the rest of the system (panel audit and recovery email). */
public interface IdentityEventsPort {

    /** Sign-in, failure or sign-out, or password change, of an admin (audit). */
    void adminSession(Long accountId, String action, String detail);

    /** The email with the code is sent by Alert Service (notifications). */
    void recoveryCode(String email, String name, String code, int validityMinutes);

    /** An account was deleted: the other services erase its personal data. */
    void accountDeleted(Long userId, Long userProfileId);
}
