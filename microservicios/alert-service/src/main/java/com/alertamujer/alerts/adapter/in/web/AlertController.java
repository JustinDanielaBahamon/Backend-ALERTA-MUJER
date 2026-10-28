package com.alertamujer.alerts.adapter.in.web;

import com.alertamujer.alerts.adapter.in.web.dto.AlertRequest;
import com.alertamujer.alerts.adapter.in.web.dto.AlertResponse;
import com.alertamujer.alerts.adapter.in.web.dto.LocationLogResponse;
import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.alerts.api.AlertMessageView;
import com.alertamujer.alerts.application.port.in.ManageAlertsUseCase;
import com.alertamujer.alerts.application.port.in.AlertChatUseCase;
import com.alertamujer.alerts.application.port.in.AlertRecipientsUseCase;
import com.alertamujer.alerts.application.port.in.AlertRecipientsUseCase.NotifiedContact;
import com.alertamujer.alerts.application.port.in.ManageOwnAlertsUseCase;
import com.alertamujer.alerts.application.port.in.LocationsUseCase;
import com.alertamujer.alerts.domain.model.AlertMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** REST input adapter for alerts. Only translates HTTP ↔ use cases. */
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final ManageOwnAlertsUseCase alerts;
    private final ManageAlertsUseCase administration;
    private final AlertRecipientsUseCase recipients;
    private final AlertChatUseCase chat;
    private final LocationsUseCase locations;

    public AlertController(ManageOwnAlertsUseCase alerts, ManageAlertsUseCase administration,
                           AlertRecipientsUseCase recipients, AlertChatUseCase chat,
                           LocationsUseCase locations) {
        this.alerts = alerts;
        this.administration = administration;
        this.recipients = recipients;
        this.chat = chat;
        this.locations = locations;
    }

    /** GPS route of an alert (used by the mobile alert tracking). */
    @GetMapping("/{id}/locations")
    public ResponseEntity<List<LocationLogResponse>> getAlertLocations(@PathVariable Long id,
                                                                       @AuthenticationPrincipal Long currentUserId,
                                                                       Authentication authentication) {
        return ResponseEntity.ok(locations.ofAlert(id, currentUserId, Authorization.esAdmin(authentication)).stream()
                .map(LocationLogResponse::of).toList());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminAlertView>> getAllAlerts() {
        return ResponseEntity.ok(administration.listAll());
    }

    /** Panel detail: last location and delivery status to each contact (without their data). */
    @GetMapping("/{id}/admin-detail")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ManageAlertsUseCase.AdminDetail> getAdminDetail(@PathVariable Long id) {
        return ResponseEntity.ok(administration.detail(id));
    }

    /** Changes the status of any user's alert, for example to "resolved" (attended). */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlertResponse> changeAlertStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(AlertResponse.of(administration.changeStatus(id, body.get("status"))));
    }

    @GetMapping("/me")
    public ResponseEntity<List<AlertResponse>> getMine(@AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(alerts.mine(currentUserId).stream().map(AlertResponse::of).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertResponse> getAlertById(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        return alerts.one(id, currentUserId)
                .map(a -> ResponseEntity.ok(AlertResponse.of(a)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userProfileId}")
    public ResponseEntity<List<AlertResponse>> getAlertsByUser(@PathVariable Long userProfileId,
                                                               @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(alerts.ofUser(userProfileId, currentUserId).stream().map(AlertResponse::of).toList());
    }

    @PostMapping
    public ResponseEntity<AlertResponse> createAlert(@RequestBody AlertRequest request, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(AlertResponse.of(alerts.activate(request.toNew(), currentUserId)));
    }

    /** Contacts notified in an alert and the status of each channel (alert owner only). */
    @GetMapping("/{id}/contacts")
    public ResponseEntity<List<NotifiedContact>> getAlertContacts(@PathVariable Long id,
                                                                     @AuthenticationPrincipal Long currentUserId) {
        return alerts.one(id, currentUserId)
                .map(a -> ResponseEntity.ok(recipients.contactsOf(a)))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Alert chat (owner only). after: id of the last message she already has (0 = all). */
    @GetMapping("/{id}/messages")
    public ResponseEntity<List<AlertMessageView>> getMessages(@PathVariable Long id,
                                                              @RequestParam(defaultValue = "0") Long after,
                                                              @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(chat.ownerMessages(id, currentUserId, after).stream().map(AlertController::view).toList());
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<AlertMessageView> sendMessage(@PathVariable Long id, @RequestBody MessageRequest request,
                                                        @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(view(chat.sendFromOwner(id, currentUserId, request.body())));
    }

    public record MessageRequest(String body) {}

    /** Keep the alert's evidence (not deleted after the retention days). Body: {"keep": true}. */
    @PatchMapping("/{id}/keep-evidence")
    public ResponseEntity<AlertResponse> keepEvidence(@PathVariable Long id, @RequestBody Map<String, Boolean> body,
                                                      @AuthenticationPrincipal Long currentUserId) {
        boolean keep = body != null && Boolean.TRUE.equals(body.get("keep"));
        return ResponseEntity.ok(AlertResponse.of(alerts.keepEvidence(id, keep, currentUserId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlertResponse> updateAlert(@PathVariable Long id, @RequestBody AlertRequest request,
                                                     @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(AlertResponse.of(alerts.update(id, request.toChanges(), currentUserId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        alerts.delete(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    private static AlertMessageView view(AlertMessage m) {
        return new AlertMessageView(m.getId(), m.getSenderType(), m.getSenderName(), m.getBody(), m.getCreatedAt());
    }
}
