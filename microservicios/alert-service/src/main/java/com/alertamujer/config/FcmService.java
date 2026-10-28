package com.alertamujer.config;

import java.util.List;
import java.util.Map;

public interface FcmService {

    /**
     * Emergency notice: data-only message with Android high priority and a 5-minute TTL,
     * so the app receives it even when closed and shows the full-screen alarm.
     * Returns the tokens that failed (empty if all were delivered to FCM).
     */
    List<String> sendEmergencyAlert(List<String> deviceTokens, Map<String, String> data);

    /**
     * Send a simple notification to a single device
     */
    String sendSingleNotification(String deviceToken, String title, String body, String alertId, String action);

    /**
     * Send a batch notification to several devices
     */
    List<String> sendBatchNotification(List<String> deviceTokens, String title, String body, String alertId, String action);

    /**
     * Notify when an alert is activated - send a push to all the user's devices
     */
    void notifyAlertTriggered(Long alertId, Long userProfileId);

    /**
     * Get all active FCM tokens of a user's devices
     */
    List<String> getFcmTokensByUser(Long userProfileId);

    /**
     * Notify real-time location updates
     */
    void sendLocationUpdateNotification(String alertId, String deviceToken, double latitude, double longitude);
}