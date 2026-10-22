package com.alertamujer.alerts.api;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Queries of the alerts module for the other modules. */
public interface AlertsApi {

    Optional<AlertSummary> findAlert(Long alertId);

    /** Alerts by id (missing ones are skipped). */
    Map<Long, AlertSummary> findAlerts(Collection<Long> alertIds);

    /** All alerts, by id. */
    Map<Long, AlertSummary> alertsById();

    /** All alerts with their user, newest to oldest (panel). */
    List<AdminAlertView> adminAlertViews();

    /** Number of alerts per user_profile.id. */
    Map<Long, Long> alertCountByProfile();

    long countActiveAlerts();

    /** Last automatic run that expired overdue alerts. */
    ExpirationStatus expirationStatus();

    /** Alerts in which this email was notified as an emergency contact. */
    List<Long> alertsNotifiedTo(String email);

    /** Deliveries recorded for an alert (email and push to each contact). */
    List<AlertRecipient> recipientsOf(Long alertId);

    /** Saves the result of a delivery: "sent" or "failed" (with the reason). */
    void markDelivery(Long recipientId, String status, String errorMessage);

    /** Alert chat: messages after afterId (null = all). */
    List<AlertMessageView> messagesOf(Long alertId, Long afterId);

    /** Message from a contact (from the tracking page). IllegalArgumentException if the alert ended. */
    AlertMessageView addContactMessage(Long alertId, String senderName, String body);

    /** Route of the alert, from the first point to the last. */
    List<LocationPoint> locationTrail(Long alertId);

    /** Ids of a user's alerts (user_profile.id). */
    List<Long> alertIdsOfProfile(Long userProfileId);

    /** Of those alerts, the ones the user marked to keep their evidence. */
    java.util.Set<Long> alertsKeepingEvidence(Collection<Long> alertIds);

    record ExpirationStatus(LocalDateTime lastCheck, int lastExpiredCount) {}
}
