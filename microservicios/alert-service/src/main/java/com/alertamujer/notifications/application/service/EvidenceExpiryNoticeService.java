package com.alertamujer.notifications.application.service;

import com.alertamujer.notifications.application.port.in.NoticeEvidenceExpiryUseCase;
import com.alertamujer.notifications.application.port.out.DevicesPort;
import com.alertamujer.notifications.application.port.out.EvidenceExpiryRepository;
import com.alertamujer.notifications.application.port.out.EvidenceExpiryRepository.UserWithExpiring;
import com.alertamujer.notifications.application.port.out.PushPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Once a day: the users whose evidence enters its last days get a single notice on their phone
 * ("Tus evidencias de tus alertas se borrarán en 5 días"), whatever the number of alerts. The day
 * window makes each evidence be announced only once.
 */
@Service
public class EvidenceExpiryNoticeService implements NoticeEvidenceExpiryUseCase {

    private static final Logger log = LoggerFactory.getLogger(EvidenceExpiryNoticeService.class);
    /** The notice comes this many days before the deletion. */
    static final int DAYS_BEFORE = 5;

    private final EvidenceExpiryRepository evidence;
    private final DevicesPort devices;
    private final PushPort push;
    private final int retentionDays;

    public EvidenceExpiryNoticeService(EvidenceExpiryRepository evidence, DevicesPort devices, PushPort push,
                                       @Value("${evidence.retention.days:30}") int retentionDays) {
        this.evidence = evidence;
        this.devices = devices;
        this.push = push;
        this.retentionDays = retentionDays;
    }

    @Override
    public int notice(LocalDateTime now) {
        if (retentionDays <= DAYS_BEFORE) return 0;
        LocalDateTime to = now.minusDays(retentionDays - DAYS_BEFORE);
        int told = 0;
        for (UserWithExpiring user : evidence.recordedBetween(to.minusDays(1), to)) {
            List<String> tokens = devices.tokensOf(user.userId());
            if (tokens.isEmpty()) continue;
            try {
                push.send(tokens, Map.of(
                        "tipo", "EVIDENCIAS_POR_BORRAR",
                        "dias", String.valueOf(DAYS_BEFORE),
                        "alertas", String.valueOf(user.alerts())));
                told++;
            } catch (RuntimeException e) {
                log.warn("No se pudo avisar el borrado de evidencias a la usuaria {}: {}", user.userId(), e.getMessage());
            }
        }
        if (told > 0) log.info("Avisos de evidencias por borrar enviados: {}", told);
        return told;
    }
}
