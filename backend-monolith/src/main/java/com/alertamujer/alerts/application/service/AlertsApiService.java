package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.alerts.api.AlertRecipient;
import com.alertamujer.alerts.api.LocationPoint;
import com.alertamujer.alerts.domain.model.EmergencyContact;
import com.alertamujer.alerts.infrastructure.repository.AlertContactRepository;
import com.alertamujer.alerts.infrastructure.repository.EmergencyContactRepository;
import com.alertamujer.alerts.infrastructure.repository.LocationLogRepository;
import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.alerts.api.AlertsApi;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** Implementación de la API pública de alerts sobre sus propios repositorios y servicios. */
@Service
@Transactional(readOnly = true)
public class AlertsApiService implements AlertsApi {

    private final AlertRepository alertRepository;
    private final AdminAlertService adminAlertService;
    private final AlertExpirationJob alertExpirationJob;
    private final AlertContactRepository alertContactRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final LocationLogRepository locationLogRepository;

    public AlertsApiService(AlertRepository alertRepository, AdminAlertService adminAlertService,
                            AlertExpirationJob alertExpirationJob, AlertContactRepository alertContactRepository,
                            EmergencyContactRepository emergencyContactRepository,
                            LocationLogRepository locationLogRepository) {
        this.alertRepository = alertRepository;
        this.adminAlertService = adminAlertService;
        this.alertExpirationJob = alertExpirationJob;
        this.alertContactRepository = alertContactRepository;
        this.emergencyContactRepository = emergencyContactRepository;
        this.locationLogRepository = locationLogRepository;
    }

    @Override
    public Optional<AlertSummary> findAlert(Long alertId) {
        return alertId == null ? Optional.empty() : alertRepository.findById(alertId).map(AlertsApiService::resumen);
    }

    @Override
    public Map<Long, AlertSummary> findAlerts(Collection<Long> alertIds) {
        return alertRepository.findAllById(alertIds).stream()
                .collect(Collectors.toMap(Alert::getId, AlertsApiService::resumen, (a, b) -> a));
    }

    @Override
    public Map<Long, AlertSummary> alertsById() {
        return alertRepository.findAll().stream()
                .collect(Collectors.toMap(Alert::getId, AlertsApiService::resumen, (a, b) -> a));
    }

    @Override
    public List<AdminAlertView> adminAlertViews() {
        return adminAlertService.listar();
    }

    @Override
    public Map<Long, Long> alertCountByProfile() {
        return alertRepository.findAll().stream()
                .filter(a -> a.getUserProfileId() != null)
                .collect(Collectors.groupingBy(Alert::getUserProfileId, Collectors.counting()));
    }

    @Override
    public long countActiveAlerts() {
        return alertRepository.findByStatus("active").size();
    }

    @Override
    public ExpirationStatus expirationStatus() {
        return new ExpirationStatus(alertExpirationJob.getUltimaRevision(), alertExpirationJob.getExpiradasUltimaRevision());
    }

    @Override
    public List<AlertRecipient> recipientsOf(Long alertId) {
        var envios = alertContactRepository.findByAlertId(alertId);
        Map<Long, String> nombres = emergencyContactRepository.findAllById(
                        envios.stream().map(e -> e.getEmergencyContactId()).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(EmergencyContact::getId, c -> c.getContactName() == null ? "" : c.getContactName(), (a, b) -> a));
        return envios.stream()
                .map(e -> new AlertRecipient(e.getId(), e.getEmergencyContactId(), nombres.get(e.getEmergencyContactId()),
                        e.getChannel(), e.getDestination()))
                .toList();
    }

    @Override
    @Transactional
    public void markDelivery(Long recipientId, String status, String errorMessage) {
        alertContactRepository.findById(recipientId).ifPresent(e -> {
            e.setStatus(status);
            e.setAttempts((e.getAttempts() == null ? 0 : e.getAttempts()) + 1);
            if ("sent".equals(status)) e.setSentAt(java.time.LocalDateTime.now());
            e.setErrorMessage(errorMessage == null ? null : errorMessage.length() > 500 ? errorMessage.substring(0, 500) : errorMessage);
            alertContactRepository.save(e);
        });
    }

    @Override
    public List<LocationPoint> locationTrail(Long alertId) {
        return locationLogRepository.findByAlertIdOrderByRecordedAtAsc(alertId).stream()
                .map(l -> new LocationPoint(l.getLatitude(), l.getLongitude(), l.getAccuracy(), l.getRecordedAt()))
                .toList();
    }

    static AlertSummary resumen(Alert a) {
        return new AlertSummary(a.getId(), a.getUserProfileId(), a.getAlertType(), a.getActivationMethod(),
                a.getStatus(), a.getMessage(), a.getStartedAt(), a.getLatitude(), a.getLongitude());
    }
}
