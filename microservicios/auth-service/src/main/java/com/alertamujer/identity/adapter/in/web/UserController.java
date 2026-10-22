package com.alertamujer.identity.adapter.in.web;

import com.alertamujer.identity.adapter.in.web.dto.UserResponse;
import com.alertamujer.identity.application.port.in.MiCuentaUseCase;
import com.alertamujer.identity.application.port.in.MiCuentaUseCase.UsuariaConFoto;
import com.alertamujer.identity.domain.model.CambiosUsuaria;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/** Adaptador de entrada REST: los datos de la persona en sesión. */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final MiCuentaUseCase miCuenta;

    public UserController(MiCuentaUseCase miCuenta) {
        this.miCuenta = miCuenta;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(Authentication authentication) {
        return ResponseEntity.ok(respuesta(miCuenta.mia((Long) authentication.getPrincipal())));
    }

    /** Cambios parciales: solo las claves enviadas; una clave con null o "" borra el dato. */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(Authentication authentication, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(respuesta(miCuenta.actualizar((Long) authentication.getPrincipal(), cambios(updates))));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(Authentication authentication, @RequestBody ChangePasswordRequest request) {
        miCuenta.cambiarContrasena((Long) authentication.getPrincipal(), request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    public record ChangePasswordRequest(String currentPassword, String newPassword) {}

    /** La usuaria elimina su cuenta: hay que enviar {"confirmation": "ELIMINAR"}. */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(Authentication authentication, @RequestBody DeleteAccountRequest request) {
        miCuenta.eliminarMiCuenta((Long) authentication.getPrincipal(), request.confirmation());
        return ResponseEntity.noContent().build();
    }

    public record DeleteAccountRequest(String confirmation) {}

    private static UserResponse respuesta(UsuariaConFoto u) {
        return UserResponse.of(u.usuaria(), u.foto());
    }

    static CambiosUsuaria cambios(Map<String, Object> updates) {
        return new CambiosUsuaria(texto(updates, "firstName"), texto(updates, "lastName"), texto(updates, "telephone"),
                texto(updates, "documentNumber"), texto(updates, "documentType"), date(updates.get("birthdate")),
                texto(updates, "profilePhoto"));
    }

    // null = la clave no llegó; Optional vacío = llegó con null
    private static Optional<String> texto(Map<String, Object> updates, String key) {
        if (!updates.containsKey(key)) return null;
        Object valor = updates.get(key);
        return Optional.ofNullable(valor == null ? null : String.valueOf(valor));
    }

    // AAAA-MM-DD; una fecha con otro formato se ignora
    private static LocalDate date(Object valor) {
        if (valor == null) return null;
        try {
            return LocalDate.parse(String.valueOf(valor));
        } catch (RuntimeException e) {
            return null;
        }
    }
}
