package com.alertamujer.identity.infrastructure.controller;

import com.alertamujer.identity.application.service.AuthService;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.infrastructure.controller.dto.AuthResponse;
import com.alertamujer.identity.infrastructure.controller.dto.ForgotPasswordRequest;
import com.alertamujer.identity.infrastructure.controller.dto.LoginRequest;
import com.alertamujer.identity.infrastructure.controller.dto.RegisterRequest;
import com.alertamujer.identity.infrastructure.controller.dto.ResetPasswordRequest;
import com.alertamujer.identity.infrastructure.controller.mapper.UserMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    public AuthController(AuthService authService, UserMapper userMapper) {
        this.authService = authService;
        this.userMapper = userMapper;
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
        
        return ResponseEntity.ok(AuthResponse.builder()
            .token(token)
            .user(userMapper.toResponse(user))
            .build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok("Si el email existe, se enviará un enlace de recuperación");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        // TODO: Implementar reset password real con token
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }
}
