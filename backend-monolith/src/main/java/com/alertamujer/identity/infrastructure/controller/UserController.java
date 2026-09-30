package com.alertamujer.identity.infrastructure.controller;

import com.alertamujer.identity.application.service.UserService;
import com.alertamujer.identity.domain.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMe(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getMe(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateMe(Authentication authentication, @RequestBody Map<String, Object> updates) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(userService.updateMe(userId, updates));
    }
}
