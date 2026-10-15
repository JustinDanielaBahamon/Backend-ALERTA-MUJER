package com.alertamujer.identity.infrastructure.controller;

import com.alertamujer.identity.application.service.AuthService;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.infrastructure.controller.dto.AuthResponse;
import com.alertamujer.identity.infrastructure.controller.dto.ForgotPasswordRequest;
import com.alertamujer.identity.infrastructure.controller.dto.GoogleLoginRequest;
import com.alertamujer.identity.infrastructure.controller.dto.LoginRequest;
import com.alertamujer.identity.infrastructure.controller.dto.RegisterRequest;
import com.alertamujer.identity.infrastructure.controller.dto.ResetPasswordRequest;
import com.alertamujer.identity.infrastructure.controller.mapper.UserMapper;
import com.alertamujer.identity.infrastructure.security.GoogleIdentityVerifier;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map; // <-- Importación necesaria

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;
    private final GoogleIdentityVerifier googleIdentityVerifier;

    public AuthController(AuthService authService, UserMapper userMapper, GoogleIdentityVerifier googleIdentityVerifier) {
        this.authService = authService;
        this.userMapper = userMapper;
        this.googleIdentityVerifier = googleIdentityVerifier;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(
            request.getNombre(),
            request.getEmail(),
            request.getPassword(),
            request.getTelefono()
        );
        
        String token = authService.generateTokenForUser(user);
        
        return ResponseEntity.ok(AuthResponse.builder()
            .token(token)
            .user(userMapper.toResponse(user))
            .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        var result = authService.login(request.getEmail(), request.getPassword());
        
        User user = (User) result.get("user");
        String token = (String) result.get("token");
        String refreshToken = (String) result.get("refreshToken"); // Extraemos el Refresh Token
        
        return ResponseEntity.ok(AuthResponse.builder()
            .token(token)
            .refreshToken(refreshToken) // Lo incluimos en la respuesta al cliente
            .user(userMapper.toResponse(user))
            .build());
    }

    /** Inicio de sesión con Google: acepta el token de Firebase (web) o de Google (móvil). */
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        var identity = googleIdentityVerifier.verify(request.getIdToken());
        var result = authService.loginWithGoogle(identity.email(), identity.name(), identity.givenName(), identity.familyName());

        return ResponseEntity.ok(AuthResponse.builder()
            .token((String) result.get("token"))
            .refreshToken((String) result.get("refreshToken"))
            .user(userMapper.toResponse((User) result.get("user")))
            .build());
    }

    /** Deja constancia del cierre de sesión (el cliente borra el token). Responde 204 aunque no haya sesión. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(org.springframework.security.core.Authentication authentication,
                                       @RequestBody(required = false) Map<String, String> body) {
        Long userId = authentication != null && authentication.getPrincipal() instanceof Long id ? id : null;
        authService.logout(userId, body == null ? null : body.get("reason"));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok("Si el email existe, se enviará un enlace de recuperación");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", "El campo refreshToken es obligatorio"
            ));
        }

        try {
            Map<String, Object> tokens = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "status", 401,
                "error", "Unauthorized",
                "message", e.getMessage()
            ));
        }
    }
}