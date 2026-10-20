package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.ChatAlertaUseCase;
import com.alertamujer.alerts.application.port.out.AlertaRepositorio;
import com.alertamujer.alerts.application.port.out.IdentidadPort;
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
    private final AlertaRepositorio alertas;
    private final IdentidadPort identidad;

    public ChatAlertaService(MensajeRepositorio mensajes, AlertaRepositorio alertas, IdentidadPort identidad) {
        this.mensajes = mensajes;
        this.alertas = alertas;
        this.identidad = identidad;
    }

    @Override
    public List<AlertMessage> mensajes(Long alertId, Long afterId) {
        return mensajes.posteriores(alertId, afterId == null ? 0L : afterId);
    }

    @Override
    public List<AlertMessage> mensajesDeLaDuena(Long alertId, Long userId, Long afterId) {
        alertaPropia(alertId, userId);
        return mensajes(alertId, afterId);
    }

    @Override
    @Transactional
    public AlertMessage enviarDeLaDuena(Long alertId, Long userId, String texto) {
        Alert alerta = alertaPropia(alertId, userId);
        String nombre = identidad.usuaria(userId).map(UserSummary::firstName).orElse(null);
        return guardar(alerta, AlertMessage.DE_LA_DUENA, nombre, texto);
    }

    @Override
    @Transactional
    public AlertMessage enviarDeContacto(Long alertId, String nombre, String texto) {
        Alert alerta = alertas.buscar(alertId).orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        return guardar(alerta, AlertMessage.DE_CONTACTO, nombre, texto);
    }

    private AlertMessage guardar(Alert alerta, String tipo, String nombre, String texto) {
        return mensajes.guardar(AlertMessage.escribir(alerta, tipo, nombre, texto, mensajes.contar(alerta.getId())));
    }

    private Alert alertaPropia(Long alertId, Long userId) {
        Alert alerta = alertas.buscar(alertId).orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        // Una alerta ajena se responde igual que una inexistente
        if (!alerta.esDe(identidad.perfilDe(userId).orElse(null))) throw new ResourceNotFoundException("Alert", alertId);
        return alerta;
    }
}
