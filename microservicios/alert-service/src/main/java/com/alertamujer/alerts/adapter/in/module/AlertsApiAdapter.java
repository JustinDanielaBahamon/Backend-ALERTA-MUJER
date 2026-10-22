package com.alertamujer.alerts.adapter.in.module;

import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.alerts.api.AlertMessageView;
import com.alertamujer.alerts.api.AlertRecipient;
import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.alerts.api.LocationPoint;
import com.alertamujer.alerts.application.port.in.ManageAlertsUseCase;
import com.alertamujer.alerts.application.port.in.AlertChatUseCase;
import com.alertamujer.alerts.application.port.in.QueryAlertsUseCase;
import com.alertamujer.alerts.application.port.in.AlertRecipientsUseCase;
import com.alertamujer.alerts.application.port.in.ExpireAlertsUseCase;
import com.alertamujer.alerts.application.port.in.LocationsUseCase;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.AlertMessage;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Input adapter: the AlertsApi contract used by the other modules (notifications) and the other
 * microservices (Auth and Evidence, through /internal/rpc). Translates the domain to the contract records.
 */
@Component
@Transactional(readOnly = true)
class AlertsApiAdapter implements AlertsApi {

    private final QueryAlertsUseCase queries;
    private final ManageAlertsUseCase administration;
    private final ExpireAlertsUseCase expiration;
    private final AlertRecipientsUseCase recipients;
    private final AlertChatUseCase chat;
    private final LocationsUseCase locations;

    AlertsApiAdapter(QueryAlertsUseCase queries, ManageAlertsUseCase administration,
                     ExpireAlertsUseCase expiration, AlertRecipientsUseCase recipients,
                     AlertChatUseCase chat, LocationsUseCase locations) {
        this.queries = queries;
        this.administration = administration;
        this.expiration = expiration;
        this.recipients = recipients;
        this.chat = chat;
        this.locations = locations;
    }

    @Override
    public Optional<AlertSummary> findAlert(Long alertId) {
        return queries.find(alertId).map(AlertsApiAdapter::summary);
    }

    @Override
    public Map<Long, AlertSummary> findAlerts(Collection<Long> alertIds) {
        return byId(queries.findMany(alertIds));
    }

    @Override
    public Map<Long, AlertSummary> alertsById() {
        return byId(queries.all());
    }

    @Override
    public List<AdminAlertView> adminAlertViews() {
        return administration.listAll();
    }

    @Override
    public Map<Long, Long> alertCountByProfile() {
        return queries.countByProfile();
    }

    @Override
    public long countActiveAlerts() {
        return queries.activeCount();
    }

    @Override
    public ExpirationStatus expirationStatus() {
        return new ExpirationStatus(expiration.lastRun(), expiration.expiredLastRun());
    }

    @Override
    public List<Long> alertsNotifiedTo(String email) {
        return recipients.alertsNotifiedTo(email);
    }

    @Override
    public List<AlertRecipient> recipientsOf(Long alertId) {
        return recipients.recipients(alertId);
    }

    @Override
    @Transactional
    public void markDelivery(Long recipientId, String status, String errorMessage) {
        recipients.recordDelivery(recipientId, status, errorMessage);
    }

    @Override
    public List<AlertMessageView> messagesOf(Long alertId, Long afterId) {
        return chat.messages(alertId, afterId).stream().map(AlertsApiAdapter::view).toList();
    }

    @Override
    @Transactional
    public AlertMessageView addContactMessage(Long alertId, String senderName, String body) {
        return view(chat.sendFromContact(alertId, senderName, body));
    }

    @Override
    public List<LocationPoint> locationTrail(Long alertId) {
        return locations.trip(alertId).stream()
                .map(l -> new LocationPoint(l.getLatitude(), l.getLongitude(), l.getAccuracy(), l.getRecordedAt()))
                .toList();
    }

    @Override
    public List<Long> alertIdsOfProfile(Long userProfileId) {
        return queries.profileIds(userProfileId);
    }

    @Override
    public Set<Long> alertsKeepingEvidence(Collection<Long> alertIds) {
        return queries.keepEvidence(alertIds);
    }

    static AlertSummary summary(Alert a) {
        return new AlertSummary(a.getId(), a.getUserProfileId(), a.getAlertType(), a.getActivationMethod(),
                a.getStatus(), a.getMessage(), a.getStartedAt(), a.getLatitude(), a.getLongitude(),
                a.getEndedAt(), a.getCancelledAt());
    }

    private static Map<Long, AlertSummary> byId(List<Alert> alerts) {
        return alerts.stream().collect(Collectors.toMap(Alert::getId, AlertsApiAdapter::summary, (a, b) -> a));
    }

    private static AlertMessageView view(AlertMessage m) {
        return new AlertMessageView(m.getId(), m.getSenderType(), m.getSenderName(), m.getBody(), m.getCreatedAt());
    }
}
