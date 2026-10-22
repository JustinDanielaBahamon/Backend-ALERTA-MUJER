package com.alertamujer.admin.application.port.in;

/**
 * Security events of admin accounts (sign-ins and sign-outs, failed attempts,
 * password change). Admin accounts only: app users do not fill the audit.
 */
public interface RecordSecurityEventUseCase {

    /** Never fails: an audit error must not prevent signing in. */
    void registrar(Long accountId, String action, String detail);
}
