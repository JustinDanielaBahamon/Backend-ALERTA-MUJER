package com.alertamujer.notifications.infrastructure.controller;

import com.alertamujer.notifications.application.ReceivedAlertsService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Alertas de las personas que tienen a la usuaria como contacto de emergencia. */
@RestController
@RequestMapping("/api/received-alerts")
public class ReceivedAlertsController {

    private final ReceivedAlertsService receivedAlertsService;

    public ReceivedAlertsController(ReceivedAlertsService receivedAlertsService) {
        this.receivedAlertsService = receivedAlertsService;
    }

    @GetMapping
    public ResponseEntity<List<ReceivedAlertsService.ReceivedAlert>> recibidas(@AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(receivedAlertsService.recibidas(currentUserId, LocalDateTime.now()));
    }

    /** Código para abrir /seguimiento/{codigo} de una alerta recibida. */
    @PostMapping("/{alertId}/tracking")
    public ResponseEntity<Map<String, String>> seguimiento(@PathVariable Long alertId,
                                                           @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(Map.of("code", receivedAlertsService.enlaceSeguimiento(currentUserId, alertId, LocalDateTime.now())));
    }
}
