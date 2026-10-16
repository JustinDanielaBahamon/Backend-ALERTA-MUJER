package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.api.AdminAlertView;
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

    public AlertsApiService(AlertRepository alertRepository, AdminAlertService adminAlertService,
                            AlertExpirationJob alertExpirationJob) {
        this.alertRepository = alertRepository;
        this.adminAlertService = adminAlertService;
        this.alertExpirationJob = alertExpirationJob;
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

    static AlertSummary resumen(Alert a) {
        return new AlertSummary(a.getId(), a.getUserProfileId(), a.getAlertType(), a.getActivationMethod(),
                a.getStatus(), a.getMessage(), a.getStartedAt(), a.getLatitude(), a.getLongitude());
    }
}
