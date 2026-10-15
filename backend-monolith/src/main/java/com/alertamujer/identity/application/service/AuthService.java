package com.alertamujer.identity.application.service;

import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.Role;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.identity.infrastructure.repository.AccountRepository;
import com.alertamujer.identity.infrastructure.repository.RoleRepository;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import com.alertamujer.shared.exception.ConflictException;
import com.alertamujer.shared.exception.InvalidCredentialsException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // Nuevo: Validación de fortaleza de contraseña
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])" +            // Al menos un dígito
        "(?=.*[!@#$%^&*])" +        // Al menos un carácter especial
        "(?=.*[a-z])" +             // Al menos una minúscula
        "(?=.*[A-Z])" +             // Al menos una mayúscula
        "(?=\\S+$)" +               // No espacios en blanco
        ".{8,}$"                    // Mínimo 8 caracteres
    );

    private boolean validarPasswordFuerte(String password) {
        Matcher matcher = PASSWORD_PATTERN.matcher(password);
        return matcher.matches();
    }

    // Método auxiliar para obtener mensaje de error
    private String getPasswordErrorMessage() {
        return "La contraseña debe tener mínimo 8 caracteres, al menos un número, " +
               "al menos un carácter especial (!@#$%^&*) y sin espacios en blanco";
    }

    public User register(String nombre, String email, String password, String telefono) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists");
        }

        // Validar fortaleza de contraseña
        if (!validarPasswordFuerte(password)) {
            throw new ConflictException(getPasswordErrorMessage());
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
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        Account account = accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if ("blocked".equals(account.getStatus())) {
            throw new InvalidCredentialsException("Account blocked");
        }

        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        account.setLastAccess(LocalDateTime.now());
        accountRepository.save(account);

        String role = user.getRole() != null && user.getRole().getId() == 2L ? "admin" : "user";
        String accessToken = generateToken(user.getId(), role);
        String refreshToken = generateRefreshToken(user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("token", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("user", user);
        return response;
    }

    // ... resto del código sin cambios
