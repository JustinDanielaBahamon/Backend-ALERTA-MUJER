package com.alertamujer.alerts.application.service;

import com.alertamujer.admin.application.service.AdminSettingsService;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Marca como "expired" las alertas que siguen activas más allá de la duración máxima
 * configurada por la administración, para que no queden abiertas para siempre.
 */
@Component
public class AlertExpirationJob {

    private static final Logger log = LoggerFactory.getLogger(AlertExpirationJob.class);

    private final AlertRepository alertRepository;
    private final AdminSettingsService settingsService;

    public AlertExpirationJob(AlertRepository alertRepository, AdminSettingsService settingsService) {
        this.alertRepository = alertRepository;
        this.settingsService = settingsService;
    }

    // La transacción va aquí: una llamada interna no pasaría por el proxy de expirarVencidas
    @Scheduled(fixedDelayString = "${alerts.expiration.check-ms:60000}", initialDelay = 30000)
    @Transactional
    public void revisar() {
        int expiradas = expirarVencidas(LocalDateTime.now());
        if (expiradas > 0) log.info("Alertas expiradas por superar la duración máxima: {}", expiradas);
    }

    public int expirarVencidas(LocalDateTime ahora) {
        int minutos = settingsService.duracionMaximaAlertaMinutos();
        if (minutos <= 0) return 0;
        LocalDateTime limite = ahora.minusMinutes(minutos);
        List<Alert> vencidas = alertRepository.findByStatus("active").stream()
                .filter(a -> a.getStartedAt() != null && a.getStartedAt().isBefore(limite))
                .toList();
        vencidas.forEach(a -> {
            a.setStatus("expired");
            a.setEndedAt(ahora);
        });
        alertRepository.saveAll(vencidas);
        return vencidas.size();
    }
}
