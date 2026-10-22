package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.AlertChatUseCase;
import com.alertamujer.alerts.application.port.out.AlertRepository;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.application.port.out.MessageRepository;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.AlertMessage;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Chat of an alert between whoever activated it and her contacts. */
@Service
public class AlertChatService implements AlertChatUseCase {

    private final MessageRepository messages;
    private final AlertRepository alerts;
    private final IdentityPort identity;

    public AlertChatService(MessageRepository messages, AlertRepository alerts, IdentityPort identity) {
        this.messages = messages;
        this.alerts = alerts;
        this.identity = identity;
    }

    @Override
    public List<AlertMessage> messages(Long alertId, Long afterId) {
        return messages.posteriores(alertId, afterId == null ? 0L : afterId);
    }

    @Override
    public List<AlertMessage> ownerMessages(Long alertId, Long userId, Long afterId) {
        ownAlert(alertId, userId);
        return messages(alertId, afterId);
    }

    @Override
    @Transactional
    public AlertMessage sendFromOwner(Long alertId, Long userId, String text) {
        Alert alert = ownAlert(alertId, userId);
        String name = identity.user(userId).map(UserSummary::firstName).orElse(null);
        return save(alert, AlertMessage.FROM_OWNER, name, text);
    }

    @Override
    @Transactional
    public AlertMessage sendFromContact(Long alertId, String name, String text) {
        Alert alert = alerts.find(alertId).orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        return save(alert, AlertMessage.FROM_CONTACT, name, text);
    }

    private AlertMessage save(Alert alert, String type, String name, String text) {
        return messages.save(AlertMessage.write(alert, type, name, text, messages.count(alert.getId())));
    }

    private Alert ownAlert(Long alertId, Long userId) {
        Alert alert = alerts.find(alertId).orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        // Someone else's alert is answered like a missing one
        if (!alert.esDe(identity.profileOf(userId).orElse(null))) throw new ResourceNotFoundException("Alert", alertId);
        return alert;
    }
}
