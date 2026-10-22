package com.alertamujer.notifications.application.port.out;

import com.alertamujer.alerts.api.AlertMessageView;
import com.alertamujer.alerts.api.AlertRecipient;
import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.LocationPoint;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Output port to the alerts module: alert data, deliveries, route and chat. */
public interface AlertsPort {

    Optional<AlertSummary> find(Long alertId);

    List<AlertSummary> findMany(Collection<Long> alertIds);

    /** Deliveries recorded for the alert (email and push to each contact). */
    List<AlertRecipient> recipients(Long alertId);

    /** "sent" or "failed" with the reason. */
    void recordDelivery(Long recipientId, String status, String error);

    /** Alerts in which this email was notified as an emergency contact. */
    List<Long> notifiedTo(String email);

    List<LocationPoint> route(Long alertId);

    List<AlertMessageView> messages(Long alertId, Long afterId);

    AlertMessageView contactMessage(Long alertId, String name, String text);
}
