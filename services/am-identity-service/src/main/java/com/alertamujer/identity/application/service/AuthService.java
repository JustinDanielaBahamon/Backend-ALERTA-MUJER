package com.alertamujer.identity.application.service;

import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.infrastructure.repository.AccountRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecretKey jwtKey;

    public AuthService(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        String secret = System.getenv().getOrDefault("JWT_SECRET", "alerta_mujer_jwt_secret_2026");
        this.jwtKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Map<String, Object> register(AuthController.RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setNombre(request.nombre());
        user.setEmail(request.email());
        user.setCorreo(request.email());
        user.setTelefono(request.telefono());
        user.setFirstName(request.nombre().split(" ")[0]);
        user.setLastName(request.nombre().split(" ").length > 1 ? request.nombre().split(" ")[1] : "");
        user = userRepository.save(user);

        Account account = new Account();
        account.setUserId(user.getId());
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setStatus("active");
        accountRepository.save(account);

        String token = generateToken(user.getId(), "user");

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", user);
        return response;
    }

    public Map<String, Object> login(AuthController.LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        Account account = accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if ("blocked".equals(account.getStatus())) {
            throw new RuntimeException("Account blocked");
        }

        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        account.setLastAccess(new Date());
        accountRepository.save(account);

        String token = generateToken(user.getId(), "Admin".equals(user.getRol()) ? "admin" : "user");

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", user);
        return response;
    }

    public Map<String, String> forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            // In production: send email with reset token
            // For now, just log it
            System.out.println("Password reset requested for: " + email);
        });
        return Map.of("message", "If the email exists, a reset link will be sent");
    }

    public Map<String, String> resetPassword(String token, String newPassword) {
        // In production: validate token from database
        // For now, just return success
        return Map.of("message", "Password updated successfully");
    }

    private String generateToken(Long userId, String role) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(jwtKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
