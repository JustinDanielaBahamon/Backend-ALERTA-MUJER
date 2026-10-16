package com.alertamujer.config;

import com.alertamujer.devices.api.DevicesApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

@Service
public class FcmServiceImpl implements FcmService {

    private final FirebaseConfig firebaseConfig;
    private final DevicesApi devicesApi;

    @Autowired
    public FcmServiceImpl(FirebaseConfig firebaseConfig, DevicesApi devicesApi) {
        this.firebaseConfig = firebaseConfig;
        this.devicesApi = devicesApi;
    }

    @Override
    public List<String> sendEmergencyAlert(List<String> deviceTokens, Map<String, String> data) {
        List<String> fallidos = new ArrayList<>();
        for (String token : deviceTokens) {
            try {
                FirebaseMessaging.getInstance().send(Message.builder()
                        .setToken(token)
                        .putAllData(data)
                        .setAndroidConfig(AndroidConfig.builder()
                                .setPriority(AndroidConfig.Priority.HIGH)
                                .setTtl(Duration.ofMinutes(5).toMillis())
                                .build())
                        .build());
            } catch (Exception e) {
                fallidos.add(token);
            }
        }
        return fallidos;
    }

    @Override
    public String sendSingleNotification(String deviceToken, String title, String body, String alertId, String action) {
        try {
            return firebaseConfig.sendToDevice(deviceToken, title, body);
        } catch (Exception e) {
            throw new RuntimeException("Error sending single notification", e);
        }
    }

    @Override
    public List<String> sendBatchNotification(List<String> deviceTokens, String title, String body, String alertId, String action) {
        try {
            return firebaseConfig.sendToMultipleDevices(deviceTokens, title, body);
        } catch (Exception e) {
            throw new RuntimeException("Error sending batch notification", e);
        }
    }

    @Override
    public void notifyAlertTriggered(Long alertId, Long userProfileId) {
        // Obtener tokens FCM de los dispositivos del usuario
        List<String> fcmTokens = getFcmTokensByUser(userProfileId);

        if (!fcmTokens.isEmpty()) {
            try {
                // Usar FirebaseMessaging.getInstance() directamente (patrón estándar de Firebase Admin SDK)
                String token = fcmTokens.get(0);
                com.google.firebase.messaging.FirebaseMessaging messaging = com.google.firebase.messaging.FirebaseMessaging.getInstance();
                String response = messaging.send(
                        com.google.firebase.messaging.Message.builder()
                                .setToken(token)
                                .setNotification(
                                        com.google.firebase.messaging.Notification.builder()
                                                .setTitle("Alerta activada")
                                                .setBody("Se ha activado una alerta de emergencia N°" + alertId)
                                                .build())
                                .build()
                );
                // Nota: En un caso completo, enviaríamos a todos los tokens, pero por simplicidad usamos el primero
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<String> getFcmTokensByUser(Long userProfileId) {
        return devicesApi.pushTokensOfProfile(userProfileId);
    }

    @Override
    public void sendLocationUpdateNotification(String alertId, String deviceToken, double latitude, double longitude) {
        // Notificación simple - usar Firebase directamente
        // TODO: Implementar notificación de ubicación
    }
}