package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.ChatAlertaUseCase;
import com.alertamujer.alerts.application.port.out.AlertaRepositorio;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.application.port.out.MensajeRepositorio;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.AlertMessage;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Chat de una alerta entre quien la activó y sus contactos. */
@Service
public class ChatAlertaService implements ChatAlertaUseCase {

    private final MensajeRepositorio mensajes;
    private final AlertaRepositorio alerts;
    private final IdentityPort identity;

    public ChatAlertaService(MensajeRepositorio mensajes, AlertaRepositorio alerts, IdentityPort identity) {
        this.mensajes = mensajes;
        this.alerts = alerts;
        this.identity = identity;
    }

    @Override
    public List<AlertMessage> mensajes(Long alertId, Long afterId) {
        return mensajes.posteriores(alertId, afterId == null ? 0L : afterId);
    }

    @Override
    public List<AlertMessage> mensajesDeLaDuena(Long alertId, Long userId, Long afterId) {
        ownAlert(alertId, userId);
        return mensajes(alertId, afterId);
    }

    @Override
    @Transactional
    public AlertMessage enviarDeLaDuena(Long alertId, Long userId, String text) {
        Alert alert = ownAlert(alertId, userId);
        String name = identity.user(userId).map(UserSummary::firstName).orElse(null);
        return save(alert, AlertMessage.DE_LA_DUENA, name, text);
    }

    @Override
    @Transactional
    public AlertMessage enviarDeContacto(Long alertId, String name, String text) {
        Alert alert = alerts.find(alertId).orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        return save(alert, AlertMessage.DE_CONTACTO, name, text);
    }

    private AlertMessage save(Alert alert, String type, String name, String text) {
        return mensajes.save(AlertMessage.escribir(alert, type, name, text, mensajes.count(alert.getId())));
    }

    private Alert ownAlert(Long alertId, Long userId) {
        Alert alert = alerts.find(alertId).orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        // Una alerta ajena se responde igual que una inexistente
        if (!alert.esDe(identity.profileOf(userId).orElse(null))) throw new ResourceNotFoundException("Alert", alertId);
        return alert;
    }
}
