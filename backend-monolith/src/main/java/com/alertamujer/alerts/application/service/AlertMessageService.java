package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.api.AlertMessageView;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.domain.model.AlertMessage;
import com.alertamujer.alerts.infrastructure.repository.AlertMessageRepository;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Chat de una alerta: quien la activó y sus contactos se escriben mientras está activa (mensajes
 * rápidos predeterminados o texto libre). Los contactos llegan por su enlace de seguimiento.
 */
@Service
public class AlertMessageService {

    static final int MAXIMO_LARGO = 500;
    static final int MAXIMO_NOMBRE = 60;
    static final int MAXIMO_POR_ALERTA = 500;

    private final AlertMessageRepository repository;
    private final AlertRepository alertRepository;
    private final IdentityApi identityApi;

    public AlertMessageService(AlertMessageRepository repository, AlertRepository alertRepository, IdentityApi identityApi) {
        this.repository = repository;
        this.alertRepository = alertRepository;
        this.identityApi = identityApi;
    }

    /** Mensajes posteriores a afterId (null o 0 = todos). */
    public List<AlertMessageView> mensajes(Long alertId, Long afterId) {
        return repository.findByAlertIdAndIdGreaterThanOrderByIdAsc(alertId, afterId == null ? 0L : afterId).stream()
                .map(AlertMessageService::vista)
                .toList();
    }

    public List<AlertMessageView> mensajesDeLaDuena(Long alertId, Long userId, Long afterId) {
        alertaPropia(alertId, userId);
        return mensajes(alertId, afterId);
    }

    @Transactional
    public AlertMessageView enviarDeLaDuena(Long alertId, Long userId, String texto) {
        Alert alerta = alertaPropia(alertId, userId);
        String nombre = identityApi.findUser(userId).map(UserSummary::firstName).orElse(null);
        return guardar(alerta, "owner", nombre, texto);
    }

    @Transactional
    public AlertMessageView enviarDeContacto(Long alertId, String nombre, String texto) {
        Alert alerta = alertRepository.findById(alertId).orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        return guardar(alerta, "contact", nombre, texto);
    }

    private AlertMessageView guardar(Alert alerta, String tipo, String nombre, String texto) {
        if (!"active".equals(alerta.getStatus())) {
            throw new IllegalArgumentException("La alerta ya terminó: no se pueden enviar más mensajes");
        }
        String cuerpo = texto == null ? "" : texto.trim();
        if (cuerpo.isEmpty()) throw new IllegalArgumentException("El mensaje está vacío");
        if (cuerpo.length() > MAXIMO_LARGO) {
            throw new IllegalArgumentException("El mensaje puede tener como máximo " + MAXIMO_LARGO + " caracteres");
        }
        if (repository.countByAlertId(alerta.getId()) >= MAXIMO_POR_ALERTA) {
            throw new IllegalArgumentException("Se alcanzó el máximo de mensajes de esta alerta");
        }
        AlertMessage mensaje = new AlertMessage();
        mensaje.setAlertId(alerta.getId());
        mensaje.setSenderType(tipo);
        mensaje.setSenderName(nombreLimpio(nombre, "owner".equals(tipo) ? "Ella" : "Contacto"));
        mensaje.setBody(cuerpo);
        return vista(repository.save(mensaje));
    }

    private Alert alertaPropia(Long alertId, Long userId) {
        Alert alerta = alertRepository.findById(alertId).orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        Long perfil = identityApi.findProfileId(userId).orElse(null);
        // Una alerta ajena se responde igual que una inexistente
        if (!Objects.equals(alerta.getUserProfileId(), perfil)) throw new ResourceNotFoundException("Alert", alertId);
        return alerta;
    }

    private static String nombreLimpio(String nombre, String porDefecto) {
        String n = nombre == null ? "" : nombre.trim();
        if (n.isEmpty()) return porDefecto;
        return n.length() > MAXIMO_NOMBRE ? n.substring(0, MAXIMO_NOMBRE) : n;
    }

    private static AlertMessageView vista(AlertMessage m) {
        return new AlertMessageView(m.getId(), m.getSenderType(), m.getSenderName(), m.getBody(), m.getCreatedAt());
    }
}
