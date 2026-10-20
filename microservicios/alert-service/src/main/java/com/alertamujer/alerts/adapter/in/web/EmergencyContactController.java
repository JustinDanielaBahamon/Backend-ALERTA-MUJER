package com.alertamujer.alerts.adapter.in.web;

import com.alertamujer.alerts.adapter.in.web.dto.EmergencyContactRequest;
import com.alertamujer.alerts.adapter.in.web.dto.EmergencyContactResponse;
import com.alertamujer.alerts.application.port.in.ContactosEmergenciaUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Adaptador de entrada REST de los contactos de emergencia. */
@RestController
@RequestMapping("/api/contacts")
public class EmergencyContactController {

    private final ContactosEmergenciaUseCase contactos;

    public EmergencyContactController(ContactosEmergenciaUseCase contactos) {
        this.contactos = contactos;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmergencyContactResponse>> getAllContacts() {
        return ResponseEntity.ok(contactos.todos().stream().map(EmergencyContactResponse::of).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmergencyContactResponse> getContactById(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        return contactos.uno(id, currentUserId)
                .map(c -> ResponseEntity.ok(EmergencyContactResponse.of(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EmergencyContactResponse>> getContactsByUser(@PathVariable Long userId,
                                                                           @AuthenticationPrincipal Long currentUserId,
                                                                           Authentication authentication) {
        return ResponseEntity.ok(contactos.deUsuaria(userId, currentUserId, Autorizacion.esAdmin(authentication)).stream()
                .map(EmergencyContactResponse::of).toList());
    }

    @PostMapping
    public ResponseEntity<EmergencyContactResponse> createContact(@RequestBody EmergencyContactRequest request,
                                                                  @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(EmergencyContactResponse.of(contactos.crear(request.aDatos(), currentUserId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmergencyContactResponse> updateContact(@PathVariable Long id, @RequestBody EmergencyContactRequest request,
                                                                  @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(EmergencyContactResponse.of(contactos.actualizar(id, request.aDatos(), currentUserId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        contactos.eliminar(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
