package com.alertamujer.alerts.adapter.in.module;

import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.alerts.api.AlertMessageView;
import com.alertamujer.alerts.api.AlertRecipient;
import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.alerts.api.LocationPoint;
import com.alertamujer.alerts.application.port.in.AdministrarAlertasUseCase;
import com.alertamujer.alerts.application.port.in.ChatAlertaUseCase;
import com.alertamujer.alerts.application.port.in.ConsultarAlertasUseCase;
import com.alertamujer.alerts.application.port.in.DestinatariosAlertaUseCase;
import com.alertamujer.alerts.application.port.in.ExpirarAlertasUseCase;
import com.alertamujer.alerts.application.port.in.UbicacionesUseCase;
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
 * Adaptador de entrada: el contrato AlertsApi que usan los otros módulos (notificaciones) y los otros
 * microservicios (Auth y Evidence, por /internal/rpc). Traduce el dominio a los records del contrato.
 */
@Component
@Transactional(readOnly = true)
class AlertsApiAdapter implements AlertsApi {

    private final ConsultarAlertasUseCase queries;
    private final AdministrarAlertasUseCase administration;
    private final ExpirarAlertasUseCase expiration;
    private final DestinatariosAlertaUseCase destinatarios;
    private final ChatAlertaUseCase chat;
    private final UbicacionesUseCase ubicaciones;

    AlertsApiAdapter(ConsultarAlertasUseCase queries, AdministrarAlertasUseCase administration,
                     ExpirarAlertasUseCase expiration, DestinatariosAlertaUseCase destinatarios,
                     ChatAlertaUseCase chat, UbicacionesUseCase ubicaciones) {
        this.queries = queries;
        this.administration = administration;
        this.expiration = expiration;
        this.destinatarios = destinatarios;
        this.chat = chat;
        this.ubicaciones = ubicaciones;
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
        return queries.cuantasActivas();
    }

    @Override
    public ExpirationStatus expirationStatus() {
        return new ExpirationStatus(expiration.ultimaRevision(), expiration.expiradasUltimaRevision());
    }

    @Override
    public List<Long> alertsNotifiedTo(String email) {
        return destinatarios.alertasAvisadasA(email);
    }

    @Override
    public List<AlertRecipient> recipientsOf(Long alertId) {
        return destinatarios.destinatarios(alertId);
    }

    @Override
    @Transactional
    public void markDelivery(Long recipientId, String status, String errorMessage) {
        destinatarios.registrarEntrega(recipientId, status, errorMessage);
    }

    @Override
    public List<AlertMessageView> messagesOf(Long alertId, Long afterId) {
        return chat.mensajes(alertId, afterId).stream().map(AlertsApiAdapter::view).toList();
    }

    @Override
    @Transactional
    public AlertMessageView addContactMessage(Long alertId, String senderName, String body) {
        return view(chat.enviarDeContacto(alertId, senderName, body));
    }

    @Override
    public List<LocationPoint> locationTrail(Long alertId) {
        return ubicaciones.trayecto(alertId).stream()
                .map(l -> new LocationPoint(l.getLatitude(), l.getLongitude(), l.getAccuracy(), l.getRecordedAt()))
                .toList();
    }

    @Override
    public List<Long> alertIdsOfProfile(Long userProfileId) {
        return queries.idsDelPerfil(userProfileId);
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
