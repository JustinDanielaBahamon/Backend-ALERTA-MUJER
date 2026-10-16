package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.alerts.api.AlertEndedEvent;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Alertas para el panel de administración: los campos de la alerta más la usuaria que la
 * activó (nombre y correo), que la entidad Alert no trae.
 */
@Service
public class AdminAlertService {

    private static final Set<String> ESTADOS = Set.of("active", "resolved", "cancelled", "expired");

    private final AlertRepository alertRepository;
    private final IdentityApi identityApi;
    private final ApplicationEventPublisher events;

    public AdminAlertService(AlertRepository alertRepository, IdentityApi identityApi, ApplicationEventPublisher events) {
        this.alertRepository = alertRepository;
        this.identityApi = identityApi;
        this.events = events;
    }

    /** Todas las alertas, de la más reciente a la más antigua. */
    @Transactional(readOnly = true)
    public List<AdminAlertView> listar() {
        Map<Long, UserSummary> usuariaPorPerfil = identityApi.usersByProfileId();

        return alertRepository.findAll().stream()
                .sorted(Comparator.comparing(Alert::getStartedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(a -> {
                    UserSummary u = usuariaPorPerfil.get(a.getUserProfileId());
                    return new AdminAlertView(a.getId(), a.getUserProfileId(), u != null ? u.id() : null,
                            u != null ? u.fullName() : null, u != null ? u.email() : null,
                            a.getDeviceId(), a.getAlertType(), a.getActivationMethod(), a.getStatus(),
                            a.getMessage(), a.getLatitude(), a.getLongitude(),
                            a.getStartedAt(), a.getEndedAt(), a.getCancelledAt(), a.getCreatedAt());
                })
                .toList();
    }

    /**
     * Cambia el estado de cualquier alerta (solo administración): "resolved" marca la alerta
     * como atendida y guarda la hora de cierre; "cancelled" guarda la hora de cancelación.
     */
    @Transactional
    public Alert cambiarEstado(Long id, String estado) {
        if (estado == null || !ESTADOS.contains(estado)) {
            throw new IllegalArgumentException("Estado no válido: " + estado);
        }
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", id));
        String estadoAnterior = alert.getStatus();
        alert.setStatus(estado);
        LocalDateTime ahora = LocalDateTime.now();
        if ("resolved".equals(estado) && alert.getEndedAt() == null) alert.setEndedAt(ahora);
        if ("cancelled".equals(estado) && alert.getCancelledAt() == null) alert.setCancelledAt(ahora);
        if ("active".equals(estadoAnterior) && AlertEndedEvent.esFinal(estado)) {
            events.publishEvent(new AlertEndedEvent(alert.getId(), alert.getUserProfileId(), estado, ahora));
        }
        return alertRepository.save(alert);
    }

    /** Mismos campos que Alert (los clientes existentes siguen funcionando) más userId, userName y userEmail. */
}
