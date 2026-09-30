package com.alertamujer.identity.infrastructure.controller;

import com.alertamujer.identity.application.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.register(
            request.get("nombre"),
            request.get("email"),
            request.get("password"),
            request.get("telefono")
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.login(
            request.get("email"),
            request.get("password")
        ));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.forgotPassword(request.get("email")));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(authService.resetPassword(
            request.get("token"),
            request.get("newPassword")
        ));
    }
}
