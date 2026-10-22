package com.alertamujer.notifications.application.service;

import com.alertamujer.notifications.application.port.in.SendRecoveryCodeUseCase;
import com.alertamujer.notifications.application.port.out.EmailsPort;
import com.alertamujer.notifications.application.port.out.EmailsPort.EmailNotSentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Envía el código de recuperación de contraseña; un fallo solo queda en el log (Auth ya respondió). */
@Service
public class RecoveryCodeService implements SendRecoveryCodeUseCase {

    private static final Logger log = LoggerFactory.getLogger(RecoveryCodeService.class);

    private final EmailsPort emails;

    public RecoveryCodeService(EmailsPort emails) {
        this.emails = emails;
    }

    @Override
    public void send(String email, String name, String code, int validityMinutes) {
        try {
            emails.recoveryCode(email, name, code, validityMinutes);
        } catch (EmailNotSentException e) {
            log.warn("No se pudo enviar el código de recuperación: {}", e.getMessage());
        }
    }
}
