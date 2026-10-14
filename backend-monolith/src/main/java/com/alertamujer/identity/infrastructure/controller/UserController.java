package com.alertamujer.identity.infrastructure.controller;

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

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(userMapper.toResponse(userService.getMe(userId)));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(Authentication authentication, @RequestBody Map<String, Object> updates) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(userMapper.toResponse(userService.updateMe(userId, updates)));
    }
}
