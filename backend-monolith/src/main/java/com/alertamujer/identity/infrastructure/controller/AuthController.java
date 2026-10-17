package com.alertamujer.identity.infrastructure.controller;

import com.alertamujer.identity.application.service.AuthService;
import com.alertamujer.identity.application.service.PasswordRecoveryService;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.infrastructure.controller.dto.AuthResponse;
import com.alertamujer.identity.infrastructure.controller.dto.ForgotPasswordRequest;
import com.alertamujer.identity.infrastructure.controller.dto.GoogleLoginRequest;
import com.alertamujer.identity.infrastructure.controller.dto.LoginRequest;
import com.alertamujer.identity.infrastructure.controller.dto.RegisterRequest;
import com.alertamujer.identity.infrastructure.controller.dto.ResetPasswordRequest;
import com.alertamujer.identity.infrastructure.controller.dto.VerifyResetCodeRequest;
import com.alertamujer.identity.infrastructure.controller.mapper.UserMapper;
import com.alertamujer.identity.infrastructure.security.GoogleIdentityVerifier;
import jakarta.servlet.http.HttpServletRequest;
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
    private final PasswordRecoveryService passwordRecoveryService;

    public AuthController(AuthService authService, UserMapper userMapper, GoogleIdentityVerifier googleIdentityVerifier,
                          PasswordRecoveryService passwordRecoveryService) {
        this.authService = authService;
        this.userMapper = userMapper;
        this.googleIdentityVerifier = googleIdentityVerifier;
        this.passwordRecoveryService = passwordRecoveryService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(
            request.getNombre(),
            request.getEmail(),
            request.getPassword(),
            request.getTelefono(),
            request.getTipoDocumento(),
            request.getNumeroDocumento(),
            request.getFechaNacimiento()
        );
        
        String token = authService.generateTokenForUser(user);
        
        return ResponseEntity.ok(AuthResponse.builder()
            .token(token)
            .refreshToken(authService.generateRefreshTokenForUser(user))
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
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request,
                                                              HttpServletRequest http) {
        passwordRecoveryService.solicitar(request.getEmail(), http.getRemoteAddr());
        // Misma respuesta exista o no la cuenta
        return ResponseEntity.ok(Map.of("message", "Si el correo está registrado, te enviamos un código de 6 dígitos"));
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<Void> verifyResetCode(@Valid @RequestBody VerifyResetCodeRequest request) {
        passwordRecoveryService.verificar(request.email(), request.code());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordRecoveryService.restablecer(request.getEmail(), request.getCode(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente"));
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