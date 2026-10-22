package com.alertamujer.alerts.adapter.in.web;

import com.alertamujer.alerts.adapter.in.web.dto.EmergencyContactRequest;
import com.alertamujer.alerts.adapter.in.web.dto.EmergencyContactResponse;
import com.alertamujer.alerts.application.port.in.EmergencyContactsUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** REST input adapter for emergency contacts. */
@RestController
@RequestMapping("/api/contacts")
public class EmergencyContactController {

    private final EmergencyContactsUseCase contacts;

    public EmergencyContactController(EmergencyContactsUseCase contacts) {
        this.contacts = contacts;
    }

    /** Separate from the list: it queries Auth Service for each contact and must not slow down the screen. */
    @GetMapping("/app-status")
    public ResponseEntity<Map<Long, String>> appStatus(@AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(contacts.appStatus(currentUserId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmergencyContactResponse> getContactById(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        return contacts.one(id, currentUserId)
                .map(c -> ResponseEntity.ok(EmergencyContactResponse.of(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EmergencyContactResponse>> getContactsByUser(@PathVariable Long userId,
                                                                           @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(contacts.ofUser(userId, currentUserId).stream()
                .map(EmergencyContactResponse::of).toList());
    }

    @PostMapping
    public ResponseEntity<EmergencyContactResponse> createContact(@RequestBody EmergencyContactRequest request,
                                                                  @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(EmergencyContactResponse.of(contacts.create(request.toData(), currentUserId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmergencyContactResponse> updateContact(@PathVariable Long id, @RequestBody EmergencyContactRequest request,
                                                                  @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(EmergencyContactResponse.of(contacts.update(id, request.toData(), currentUserId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        contacts.delete(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
