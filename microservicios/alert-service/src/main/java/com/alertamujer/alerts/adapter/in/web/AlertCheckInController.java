package com.alertamujer.alerts.adapter.in.web;

import com.alertamujer.alerts.adapter.in.web.dto.AlertResponse;
import com.alertamujer.alerts.application.port.in.AlertCheckInUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST input adapter: the owner of an active alert says she is still in danger, or cancels under duress. */
@RestController
@RequestMapping("/api/alerts")
public class AlertCheckInController {

    private final AlertCheckInUseCase checkIn;

    public AlertCheckInController(AlertCheckInUseCase checkIn) {
        this.checkIn = checkIn;
    }

    /** "Sigo en peligro": the alert is extended. */
    @PostMapping("/{id}/extend")
    public ResponseEntity<AlertResponse> extend(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(AlertResponse.of(checkIn.extend(id, userId)));
    }

    /** Duress PIN: it goes on and the contacts are warned (her phone shows it as cancelled). */
    @PostMapping("/{id}/duress")
    public ResponseEntity<AlertResponse> duress(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(AlertResponse.of(checkIn.duress(id, userId)));
    }
}
