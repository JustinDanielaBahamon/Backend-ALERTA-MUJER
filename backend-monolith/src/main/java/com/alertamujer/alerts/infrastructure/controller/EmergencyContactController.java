package com.alertamujer.alerts.infrastructure.controller;

import com.alertamujer.alerts.application.service.EmergencyContactService;
import com.alertamujer.alerts.domain.model.EmergencyContact;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<EmergencyContact>> getAllContacts() {
        return ResponseEntity.ok(emergencyContactService.getAllContacts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmergencyContact> getContactById(@PathVariable Long id) {
        return emergencyContactService.getContactById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userProfileId}")
    public ResponseEntity<List<EmergencyContact>> getContactsByUser(@PathVariable Long userProfileId) {
        return ResponseEntity.ok(emergencyContactService.getContactsByUserProfile(userProfileId));
    }

    @PostMapping
    public ResponseEntity<EmergencyContact> createContact(@RequestBody EmergencyContact contact) {
        return ResponseEntity.ok(emergencyContactService.createContact(contact));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmergencyContact> updateContact(@PathVariable Long id, @RequestBody EmergencyContact contact) {
        return ResponseEntity.ok(emergencyContactService.updateContact(id, contact));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        emergencyContactService.deleteContact(id);
        return ResponseEntity.noContent().build();
    }
}
