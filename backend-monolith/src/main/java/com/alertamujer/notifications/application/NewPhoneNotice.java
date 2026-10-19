package com.alertamujer.notifications.application;

import com.alertamujer.config.FcmService;
import com.alertamujer.devices.api.NewPhoneSignedInEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/** Avisa a los otros celulares de la usuaria que se entró a su cuenta desde un celular nuevo. */
@Service
public class NewPhoneNotice {

    private static final Logger log = LoggerFactory.getLogger(NewPhoneNotice.class);

    private final FcmService fcmService;

    public NewPhoneNotice(FcmService fcmService) {
        this.fcmService = fcmService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alEntrarDesdeOtroCelular(NewPhoneSignedInEvent evento) {
        try {
            fcmService.sendEmergencyAlert(evento.otrosTokens(), Map.of("tipo", "NUEVO_CELULAR", "celular", evento.celular()));
        } catch (RuntimeException e) {
            log.warn("No se pudo avisar del nuevo celular: {}", e.getMessage());
        }
    }
}
