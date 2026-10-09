package com.alertamujer.alerts.infrastructure.controller;

import com.alertamujer.alerts.application.service.EmergencyContactService;
import com.alertamujer.alerts.domain.model.EmergencyContact;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class EmergencyContactController {

    private final EmergencyContactService emergencyContactService;

    public EmergencyContactController(EmergencyContactService emergencyContactService) {
        this.emergencyContactService = emergencyContactService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmergencyContact>> getAllContacts() {
        return ResponseEntity.ok(emergencyContactService.getAllContacts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmergencyContact> getContactById(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        return emergencyContactService.getContactById(id, currentUserId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userProfileId}")
    public ResponseEntity<List<EmergencyContact>> getContactsByUser(@PathVariable Long userProfileId) {
        return ResponseEntity.ok(emergencyContactService.getContactsByUserProfile(userProfileId));
    }

    @GetMapping("/user-id/{userId}")
    public ResponseEntity<List<EmergencyContact>> getContactsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(emergencyContactService.getContactsByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<EmergencyContact> createContact(@RequestBody EmergencyContact contact, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(emergencyContactService.createContact(contact, currentUserId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmergencyContact> updateContact(@PathVariable Long id, @RequestBody EmergencyContact contact, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(emergencyContactService.updateContact(id, contact, currentUserId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        emergencyContactService.deleteContact(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
