package com.alertamujer.identity.adapter.in.web;

import com.alertamujer.identity.adapter.in.web.dto.UserResponse;
import com.alertamujer.identity.application.port.in.MyAccountUseCase;
import com.alertamujer.identity.application.port.in.MyAccountUseCase.UserWithPhoto;
import com.alertamujer.identity.domain.model.UserChanges;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/** REST input adapter: the data of the signed-in person. */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final MyAccountUseCase myAccount;

    public UserController(MyAccountUseCase myAccount) {
        this.myAccount = myAccount;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(Authentication authentication) {
        return ResponseEntity.ok(response(myAccount.mine((Long) authentication.getPrincipal())));
    }

    /** Partial changes: only the sent keys; a key with null or "" erases the value. */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(Authentication authentication, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(response(myAccount.update((Long) authentication.getPrincipal(), changes(updates))));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(Authentication authentication, @RequestBody ChangePasswordRequest request) {
        myAccount.changePassword((Long) authentication.getPrincipal(), request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    public record ChangePasswordRequest(String currentPassword, String newPassword) {}

    /** The user deletes her account: {"confirmation": "ELIMINAR"} must be sent. */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(Authentication authentication, @RequestBody DeleteAccountRequest request) {
        myAccount.deleteMyAccount((Long) authentication.getPrincipal(), request.confirmation());
        return ResponseEntity.noContent().build();
    }

    public record DeleteAccountRequest(String confirmation) {}

    private static UserResponse response(UserWithPhoto u) {
        return UserResponse.of(u.user(), u.photo());
    }

    static UserChanges changes(Map<String, Object> updates) {
        return new UserChanges(text(updates, "firstName"), text(updates, "lastName"), text(updates, "telephone"),
                text(updates, "documentNumber"), text(updates, "documentType"), date(updates.get("birthdate")),
                text(updates, "profilePhoto"));
    }

    // null = the key did not arrive; empty Optional = it arrived as null
    private static Optional<String> text(Map<String, Object> updates, String key) {
        if (!updates.containsKey(key)) return null;
        Object value = updates.get(key);
        return Optional.ofNullable(value == null ? null : String.valueOf(value));
    }

    // YYYY-MM-DD; a date in another format is ignored
    private static LocalDate date(Object value) {
        if (value == null) return null;
        try {
            return LocalDate.parse(String.valueOf(value));
        } catch (RuntimeException e) {
            return null;
        }
    }
}
