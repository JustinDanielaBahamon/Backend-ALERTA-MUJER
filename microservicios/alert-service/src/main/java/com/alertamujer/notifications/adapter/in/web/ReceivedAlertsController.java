package com.alertamujer.notifications.adapter.in.web;

import com.alertamujer.notifications.application.port.in.ReceivedAlertsUseCase;
import com.alertamujer.notifications.application.port.in.ReceivedAlertsUseCase.ReceivedAlert;
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

/** REST input adapter: alerts of the people who have the user as an emergency contact. */
@RestController
@RequestMapping("/api/received-alerts")
public class ReceivedAlertsController {

    private final ReceivedAlertsUseCase received;

    public ReceivedAlertsController(ReceivedAlertsUseCase received) {
        this.received = received;
    }

    @GetMapping
    public ResponseEntity<List<ReceivedAlert>> received(@AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(received.received(currentUserId, LocalDateTime.now()));
    }

    /** Code to open /seguimiento/{code} of a received alert. */
    @PostMapping("/{alertId}/tracking")
    public ResponseEntity<Map<String, String>> tracking(@PathVariable Long alertId,
                                                           @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(Map.of("code", received.trackingLink(currentUserId, alertId, LocalDateTime.now())));
    }
}
