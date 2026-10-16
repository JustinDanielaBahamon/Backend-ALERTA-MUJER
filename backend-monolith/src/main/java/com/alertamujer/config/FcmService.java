package com.alertamujer.config;

import java.util.List;
import java.util.Map;

public interface FcmService {

    /**
     * Aviso de emergencia: mensaje solo de datos con prioridad alta de Android y vigencia de 5 minutos,
     * para que la app lo reciba aunque esté cerrada y muestre la alarma a pantalla completa.
     * Devuelve los tokens que fallaron (vacío si todos se entregaron a FCM).
     */
    List<String> sendEmergencyAlert(List<String> deviceTokens, Map<String, String> data);

    /**
     * Enviar notificación simple a un solo dispositivo
     */
    String sendSingleNotification(String deviceToken, String title, String body, String alertId, String action);

    /**
     * Enviar notificación en lote a múltiples dispositivos
     */
    List<String> sendBatchNotification(List<String> deviceTokens, String title, String body, String alertId, String action);

    /**
     * Notificar cuando se activa una alerta - enviar push a todos los dispositivos del usuario
     */
    void notifyAlertTriggered(Long alertId, Long userProfileId);

    /**
     * Obtener todos los tokens FCM activos de los dispositivos de un usuario
     */
    List<String> getFcmTokensByUser(Long userProfileId);

    /**
     * Notificar actualización de ubicación en tiempo real
     */
    void sendLocationUpdateNotification(String alertId, String deviceToken, double latitude, double longitude);
}