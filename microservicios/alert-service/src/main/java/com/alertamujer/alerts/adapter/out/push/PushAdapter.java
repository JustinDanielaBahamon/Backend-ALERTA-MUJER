package com.alertamujer.alerts.adapter.out.push;

import com.alertamujer.alerts.application.port.out.PushPort;
import com.alertamujer.config.FcmService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** Adaptador de salida: push de datos con Firebase Cloud Messaging. */
@Component
class PushAdapter implements PushPort {

    private final FcmService fcmService;

    PushAdapter(FcmService fcmService) {
        this.fcmService = fcmService;
    }

    @Override
    public List<String> sendData(List<String> tokens, Map<String, String> data) {
        return fcmService.sendEmergencyAlert(tokens, data);
    }
}
