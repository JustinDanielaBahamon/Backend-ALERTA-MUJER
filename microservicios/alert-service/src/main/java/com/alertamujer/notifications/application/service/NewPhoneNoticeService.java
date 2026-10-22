package com.alertamujer.notifications.application.service;

import com.alertamujer.notifications.application.port.in.NotifyNewPhoneUseCase;
import com.alertamujer.notifications.application.port.out.PushPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/** Push a los otros celulares de la usuaria cuando entra desde uno nuevo (solo a ella, no a sus contactos). */
@Service
public class NewPhoneNoticeService implements NotifyNewPhoneUseCase {

    private static final Logger log = LoggerFactory.getLogger(NewPhoneNoticeService.class);

    private final PushPort push;

    public NewPhoneNoticeService(PushPort push) {
        this.push = push;
    }

    @Override
    public void notify(List<String> otherPhoneTokens, String phone) {
        try {
            push.send(otherPhoneTokens, Map.of("tipo", "NUEVO_CELULAR", "celular", phone));
        } catch (RuntimeException e) {
            log.warn("No se pudo avisar del nuevo celular: {}", e.getMessage());
        }
    }
}
