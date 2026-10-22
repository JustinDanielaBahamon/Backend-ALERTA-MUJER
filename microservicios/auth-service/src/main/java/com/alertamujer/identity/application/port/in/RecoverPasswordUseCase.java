package com.alertamujer.identity.application.port.in;

/**
 * Password recovery with a 6-digit code sent by email. The response when requesting the
 * code is the same whether or not the account exists (it does not reveal which emails are registered).
 */
public interface RecoverPasswordUseCase {

    void request(String email, String ip);

    /** Checks the code without using it (intermediate step of the screens). Counts as an attempt. */
    void verify(String email, String code);

    void reset(String email, String code, String newPassword);
}
