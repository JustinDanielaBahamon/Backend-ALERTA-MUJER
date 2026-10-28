package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.api.AlertRecipient;
import com.alertamujer.alerts.domain.model.Alert;

import java.time.LocalDateTime;
import java.util.List;

/** Who was notified in each alert and how each delivery is going. */
public interface AlertRecipientsUseCase {

    /** Contacts notified in the alert, with the status of each channel. */
    List<NotifiedContact> contactsOf(Alert alert);

    /** Deliveries recorded for the alert (for the notifications module). */
    List<AlertRecipient> recipients(Long alertId);

    /** Result of a delivery: "sent" or "failed" (with the reason). */
    void recordDelivery(Long recipientId, String status, String error);

    /** The contact got the delivery's notice or, with seen, opened the tracking from it. */
    void markReached(Long alertId, Long recipientId, boolean seen);

    /** Alerts in which this email was notified. */
    List<Long> alertsNotifiedTo(String email);

    /** status: pending, sent, delivered, seen, failed or expired. */
    record DeliveryChannel(String channel, String destination, String status,
                      LocalDateTime sentAt, LocalDateTime deliveredAt, LocalDateTime seenAt) {}

    record NotifiedContact(Long contactId, String name, String relationship, List<DeliveryChannel> channels) {}
}
