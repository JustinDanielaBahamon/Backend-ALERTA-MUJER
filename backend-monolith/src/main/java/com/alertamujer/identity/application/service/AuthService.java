package com.alertamujer.identity.application.service;

import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.Role;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.identity.infrastructure.repository.AccountRepository;
import com.alertamujer.identity.infrastructure.repository.RoleRepository;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final UserProfileRepository userProfileRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecretKey jwtKey;

    public AuthService(UserRepository userRepository, AccountRepository accountRepository, UserProfileRepository userProfileRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.userProfileRepository = userProfileRepository;
        this.roleRepository = roleRepository;
        String secret = System.getenv().getOrDefault("JWT_SECRET", "alerta_mujer_jwt_secret_key_2026_xK9mP2qR5sT8uV1wX4yZ7");

        this.jwtKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public User register(String nombre, String email, String password, String telefono) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setFirstName(nombre.split(" ")[0]);
        user.setLastName(nombre.split(" ").length > 1 ? nombre.split(" ")[1] : "");
        user.setEmail(email);
        user.setTelephone(telefono);
        
        Role userRole = roleRepository.findById(1L).orElseGet(() -> {
            Role role = new Role();
            role.setId(1L);
            role.setName("user");
            role.setDescription("Usuario estándar de la aplicación");
            return roleRepository.save(role);
        });
        user.setRole(userRole);
        
        user = userRepository.save(user);

        Account account = new Account();
        account.setUser(user);
        account.setPasswordHash(passwordEncoder.encode(password));
        account.setStatus("active");
        accountRepository.save(account);

        // Crear también el perfil de la usuaria: sin él no puede crear alertas, contactos ni ubicaciones
        if (!userProfileRepository.existsByUserId(user.getId())) {
            UserProfile profile = new UserProfile();
            profile.setUser(user);
            userProfileRepository.save(profile);
        }

        return user;
    }

    public Map<String, Object> login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        Account account = accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if ("blocked".equals(account.getStatus())) {
            throw new RuntimeException("Account blocked");
        }

        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        account.setLastAccess(LocalDateTime.now());
        accountRepository.save(account);

        String role = user.getRole() != null && user.getRole().getId() == 2L ? "admin" : "user";
        String token = generateToken(user.getId(), role);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", user);
        return response;
    }

    public String generateTokenForUser(User user) {
        String role = user.getRole() != null && user.getRole().getId() == 2L ? "admin" : "user";
        return generateToken(user.getId(), role);
    }

    public Map<String, String> forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            System.out.println("Password reset requested for: " + email);
        });
        return Map.of("message", "If the email exists, a reset link will be sent");
    }

    public Map<String, String> resetPassword(String token, String newPassword) {
        return Map.of("message", "Password updated successfully");
    }

    private String generateToken(Long userId, String role) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(jwtKey)
                .compact();
    }
}
