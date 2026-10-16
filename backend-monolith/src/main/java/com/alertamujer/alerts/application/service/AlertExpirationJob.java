package com.alertamujer.alerts.application.service;

import com.alertamujer.settings.api.SettingsApi;
import com.alertamujer.alerts.domain.model.Alert;
import com.alertamujer.alerts.infrastructure.repository.AlertRepository;
import com.alertamujer.shared.web.AuditSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
    private final SettingsApi settingsService;

    // Última ejecución, para el estado del sistema en el panel
    private volatile LocalDateTime ultimaRevision;
    private volatile int expiradasUltimaRevision;

    public AlertExpirationJob(AlertRepository alertRepository, SettingsApi settingsService) {
        this.alertRepository = alertRepository;
        this.settingsService = settingsService;
    }

    @Scheduled(fixedDelayString = "${alerts.expiration.check-ms:60000}", initialDelay = 30000)
    public void revisar() {
        LocalDateTime ahora = LocalDateTime.now();
        // saveAll abre la transacción dentro de AuditSource.con, así la auditoría registra el origen
        int expiradas = AuditSource.con("Tarea automática · Expiración de alertas", () -> expirarVencidas(ahora));
        ultimaRevision = ahora;
        expiradasUltimaRevision = expiradas;
        if (expiradas > 0) log.info("Alertas expiradas por superar la duración máxima: {}", expiradas);
    }

    public LocalDateTime getUltimaRevision() {
        return ultimaRevision;
    }

    public int getExpiradasUltimaRevision() {
        return expiradasUltimaRevision;
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
