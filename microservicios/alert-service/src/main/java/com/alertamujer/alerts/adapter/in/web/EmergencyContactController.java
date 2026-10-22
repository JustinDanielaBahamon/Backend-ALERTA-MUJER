package com.alertamujer.alerts.adapter.in.web;

import com.alertamujer.alerts.adapter.in.web.dto.EmergencyContactRequest;
import com.alertamujer.alerts.adapter.in.web.dto.EmergencyContactResponse;
import com.alertamujer.alerts.application.port.in.ContactosEmergenciaUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Adaptador de entrada REST de los contactos de emergencia. */
@RestController
@RequestMapping("/api/contacts")
public class EmergencyContactController {

    private final ContactosEmergenciaUseCase contactos;

    public EmergencyContactController(ContactosEmergenciaUseCase contactos) {
        this.contactos = contactos;
    }

    /** Va aparte de la lista: consulta Auth Service por cada contacto y no debe demorar la pantalla. */
    @GetMapping("/app-status")
    public ResponseEntity<Map<Long, String>> appStatus(@AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(contactos.estadoApp(currentUserId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmergencyContactResponse> getContactById(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        return contactos.uno(id, currentUserId)
                .map(c -> ResponseEntity.ok(EmergencyContactResponse.of(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EmergencyContactResponse>> getContactsByUser(@PathVariable Long userId,
                                                                           @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(contactos.deUsuaria(userId, currentUserId).stream()
                .map(EmergencyContactResponse::of).toList());
    }

    @PostMapping
    public ResponseEntity<EmergencyContactResponse> createContact(@RequestBody EmergencyContactRequest request,
                                                                  @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(EmergencyContactResponse.of(contactos.create(request.aDatos(), currentUserId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmergencyContactResponse> updateContact(@PathVariable Long id, @RequestBody EmergencyContactRequest request,
                                                                  @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(EmergencyContactResponse.of(contactos.actualizar(id, request.aDatos(), currentUserId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id, @AuthenticationPrincipal Long currentUserId) {
        contactos.delete(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
