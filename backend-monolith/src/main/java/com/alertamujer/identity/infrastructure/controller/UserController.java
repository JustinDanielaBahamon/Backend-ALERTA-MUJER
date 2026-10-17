package com.alertamujer.identity.infrastructure.controller;

import com.alertamujer.identity.application.service.AuthService;
import com.alertamujer.identity.application.service.UserService;
import com.alertamujer.identity.infrastructure.controller.dto.UserResponse;
import com.alertamujer.identity.infrastructure.controller.mapper.UserMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthService authService;

    public UserController(UserService userService, UserMapper userMapper, AuthService authService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.authService = authService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(conFoto(userMapper.toResponse(userService.getMe(userId)), userId));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(Authentication authentication, @RequestBody Map<String, Object> updates) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(conFoto(userMapper.toResponse(userService.updateMe(userId, updates)), userId));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(Authentication authentication, @RequestBody ChangePasswordRequest request) {
        authService.changePassword((Long) authentication.getPrincipal(), request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    public record ChangePasswordRequest(String currentPassword, String newPassword) {}

    private UserResponse conFoto(UserResponse respuesta, Long userId) {
        respuesta.setProfilePhoto(userService.getProfilePhoto(userId));
        return respuesta;
    }
}
