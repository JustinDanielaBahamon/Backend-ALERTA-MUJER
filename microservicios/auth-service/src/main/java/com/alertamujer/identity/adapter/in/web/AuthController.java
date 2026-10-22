package com.alertamujer.identity.adapter.in.web;

import com.alertamujer.identity.adapter.in.web.dto.AuthResponse;
import com.alertamujer.identity.adapter.in.web.dto.ForgotPasswordRequest;
import com.alertamujer.identity.adapter.in.web.dto.GoogleLoginRequest;
import com.alertamujer.identity.adapter.in.web.dto.LoginRequest;
import com.alertamujer.identity.adapter.in.web.dto.RegisterRequest;
import com.alertamujer.identity.adapter.in.web.dto.ResetPasswordRequest;
import com.alertamujer.identity.adapter.in.web.dto.VerifyResetCodeRequest;
import com.alertamujer.identity.application.port.in.RegistrationData;
import com.alertamujer.identity.application.port.in.SignInUseCase;
import com.alertamujer.identity.application.port.in.RecoverPasswordUseCase;
import com.alertamujer.identity.application.port.in.RegisterUserUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** REST input adapter: registration, session and password recovery. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase record;
    private final SignInUseCase session;
    private final RecoverPasswordUseCase recovery;

    public AuthController(RegisterUserUseCase record, SignInUseCase session,
                          RecoverPasswordUseCase recovery) {
        this.record = record;
        this.session = session;
        this.recovery = recovery;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest r) {
        return ResponseEntity.ok(AuthResponse.of(record.registrar(new RegistrationData(r.getName(), r.getEmail(),
                r.getPassword(), r.getTelephone(), r.getDocumentType(), r.getDocumentNumber(), r.getBirthDate(),
                r.getAcceptsTerms()))));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(AuthResponse.of(session.withEmail(request.getEmail(), request.getPassword())));
    }

    /** Sign-in with Google: accepts the Firebase token (web) or the Google token (mobile). */
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(AuthResponse.of(session.conGoogle(request.getIdToken())));
    }

    /** Records the sign-out (the client deletes the token). Returns 204 even if there is no session. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication, @RequestBody(required = false) Map<String, String> body) {
        Long userId = authentication != null && authentication.getPrincipal() instanceof Long id ? id : null;
        session.close(userId, body == null ? null : body.get("reason"));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request,
                                                              HttpServletRequest http) {
        recovery.request(request.getEmail(), http.getRemoteAddr());
        // Same response whether or not the account exists
        return ResponseEntity.ok(Map.of("message", "Si el correo está registrado, te enviamos un código de 6 dígitos"));
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<Void> verifyResetCode(@Valid @RequestBody VerifyResetCodeRequest request) {
        recovery.verify(request.email(), request.code());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        recovery.reset(request.getEmail(), request.getCode(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400, "error", "Bad Request", "message", "El campo refreshToken es obligatorio"));
        }
        try {
            return ResponseEntity.ok(Map.of("token", session.refresh(refreshToken)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "status", 401, "error", "Unauthorized", "message", e.getMessage()));
        }
    }
}
