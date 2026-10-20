package com.alertamujer.config;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AlertWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public AlertWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcast active alert updates to /topic/alerts/active
     */
    public void broadcastAlertActive(Object alertData) {
        messagingTemplate.convertAndSend("/topic/alerts/active", alertData);
    }

    /**
     * Broadcast alert update to specific alert topic /topic/alerts/{alertId}
     */
    public void broadcastAlertById(String alertId, Object alertData) {
        messagingTemplate.convertAndSend("/topic/alerts/" + alertId, alertData);
    }

    /**
     * Stream real-time location logs to /topic/locations/{alertId}
     */
    public void broadcastLocationLog(String alertId, Object locationData) {
        messagingTemplate.convertAndSend("/topic/locations/" + alertId, locationData);
    }

    /**
     * Send location log message (fire-and-forget pattern)
     * Uses convertAndSendToUser for user-specific queues, 
     * or convertAndSend for broadcast topics
     */
    public void sendLocationLog(String alertId, Object locationData, String userId) {
        // Send to user-specific queue using the user ID
        messagingTemplate.convertAndSendToUser(userId, "/queue/locations/" + alertId, locationData);
    }
}