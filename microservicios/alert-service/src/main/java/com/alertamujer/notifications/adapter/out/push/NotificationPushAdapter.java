package com.alertamujer.notifications.adapter.out.push;

import com.alertamujer.config.FcmService;
import com.alertamujer.notifications.application.port.out.PushPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** Output adapter: high-priority push with Firebase Cloud Messaging. */
@Component
class NotificationPushAdapter implements PushPort {

    private final FcmService fcmService;

    NotificationPushAdapter(FcmService fcmService) {
        this.fcmService = fcmService;
    }

    @Override
    public List<String> send(List<String> tokens, Map<String, String> data) {
        return fcmService.sendEmergencyAlert(tokens, data);
    }
}
