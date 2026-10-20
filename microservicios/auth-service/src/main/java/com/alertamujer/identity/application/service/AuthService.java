package com.alertamujer.identity.application.service;

import com.alertamujer.identity.api.AdminSessionEvent;
import org.springframework.context.ApplicationEventPublisher;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.Role;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import com.alertamujer.identity.infrastructure.repository.AccountRepository;
import com.alertamujer.identity.infrastructure.repository.RoleRepository;
import com.alertamujer.identity.infrastructure.repository.UserProfileRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import com.alertamujer.shared.exception.DuplicateFieldsException;
import com.alertamujer.shared.exception.InvalidCredentialsException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final UserProfileRepository userProfileRepository;
    private final RoleRepository roleRepository;
    private final ApplicationEventPublisher events;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecretKey jwtKey;

    public AuthService(UserRepository userRepository, AccountRepository accountRepository, UserProfileRepository userProfileRepository,
                       RoleRepository roleRepository, ApplicationEventPublisher events,
                       com.alertamujer.shared.security.JwtKeyProvider jwtKeyProvider) {
        this.events = events;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.userProfileRepository = userProfileRepository;
        this.roleRepository = roleRepository;
        this.jwtKey = jwtKeyProvider.key();
    }

    public User register(String nombre, String email, String password, String telefono) {
        return register(nombre, email, password, telefono, null, null, null);
    }

    /**
     * Crea la cuenta. Si el correo, el teléfono o el documento ya los usa otra cuenta, responde
     * con todos los campos repetidos a la vez (para mostrar cada aviso debajo de su campo).
     */
    public User register(String nombre, String email, String password, String telefono,
                         String tipoDocumento, String numeroDocumento, String fechaNacimiento) {
        String telefonoLimpio = limpiarTelefono(telefono);
        String documento = vacioANull(numeroDocumento);

        Map<String, String> repetidos = new LinkedHashMap<>();
        if (userRepository.existsByEmail(email)) {
            repetidos.put("email", "Este correo ya está registrado");
        }
        if (telefonoLimpio != null && userRepository.existsByTelephone(telefonoLimpio)) {
            repetidos.put("telefono", "Este número de teléfono ya está registrado");
        }
        if (documento != null && userRepository.existsByDocumentNumber(documento)) {
            repetidos.put("numeroDocumento", "Este número de documento ya está registrado");
        }
        if (!repetidos.isEmpty()) {
            throw new DuplicateFieldsException(repetidos);
        }

        User user = new User();
        user.setFirstName(nombre.split(" ")[0]);
        user.setLastName(nombre.split(" ").length > 1 ? nombre.split(" ")[1] : "");
        user.setEmail(email);
        user.setTelephone(telefonoLimpio);
        user.setDocumentType(vacioANull(tipoDocumento));
        user.setDocumentNumber(documento);
        user.setBirthdate(fecha(fechaNacimiento));
        
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

    // "300 123-4567" y "3001234567" son el mismo número: se guarda sin espacios ni separadores
    private static String limpiarTelefono(String telefono) {
        return vacioANull(telefono == null ? null : telefono.replaceAll("[\\s().-]", ""));
    }

    private static String vacioANull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static LocalDate fecha(String ddMmAaaa) {
        if (ddMmAaaa == null || ddMmAaaa.isBlank()) return null;
        try {
            return LocalDate.parse(ddMmAaaa.trim(), DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public Map<String, Object> login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        Account account = accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if ("blocked".equals(account.getStatus())) {
            if (esAdmin(user)) sesionAdmin(account.getId(), AdminSessionEvent.LOGIN_FAILED, "Cuenta bloqueada");
            throw new InvalidCredentialsException("Account blocked");
        }

        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            if (esAdmin(user)) sesionAdmin(account.getId(), AdminSessionEvent.LOGIN_FAILED, "Contraseña incorrecta");
            throw new InvalidCredentialsException("Invalid credentials");
        }

        account.setLastAccess(LocalDateTime.now());
        accountRepository.save(account);
        if (esAdmin(user)) sesionAdmin(account.getId(), AdminSessionEvent.LOGIN_SUCCESS, "Con correo y contraseña");

        String role = user.getRole() != null && user.getRole().getId() == 2L ? "admin" : "user";
        String accessToken = generateToken(user.getId(), role);
        String refreshToken = generateRefreshToken(user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("token", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("user", user);
        return response;
    }

    /**
     * Inicio de sesión con Google (identidad ya verificada por GoogleIdentityVerifier).
     * Si el correo ya está registrado, entra a esa cuenta; si no, crea la usuaria y su perfil.
     * La cuenta creada no tiene una contraseña utilizable: solo puede entrar con Google
     * (o recuperar la contraseña para usar también correo y contraseña).
     */
    public Map<String, Object> loginWithGoogle(String email, String name, String givenName, String familyName) {
        User user = userRepository.findByEmail(email).orElseGet(() -> crearUsuariaDesdeGoogle(email, name, givenName, familyName));

        Account account = accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
        if ("blocked".equals(account.getStatus())) {
            if (esAdmin(user)) sesionAdmin(account.getId(), AdminSessionEvent.LOGIN_FAILED, "Cuenta bloqueada (Google)");
            throw new InvalidCredentialsException("Account blocked");
        }
        account.setLastAccess(LocalDateTime.now());
        accountRepository.save(account);
        if (esAdmin(user)) sesionAdmin(account.getId(), AdminSessionEvent.LOGIN_SUCCESS, "Con Google");

        String role = user.getRole() != null && user.getRole().getId() == 2L ? "admin" : "user";
        Map<String, Object> response = new HashMap<>();
        response.put("token", generateToken(user.getId(), role));
        response.put("refreshToken", generateRefreshToken(user.getId()));
        response.put("user", user);
        return response;
    }

    private User crearUsuariaDesdeGoogle(String email, String name, String givenName, String familyName) {
        String nombre = givenName != null && !givenName.isBlank() ? givenName : (name != null ? name.split(" ")[0] : "");
        String apellido = familyName != null ? familyName
                : (name != null && name.contains(" ") ? name.substring(name.indexOf(' ') + 1) : "");
        if (nombre.isBlank()) nombre = email.substring(0, email.indexOf('@'));

        User user = new User();
        user.setFirstName(nombre.length() > 100 ? nombre.substring(0, 100) : nombre);
        user.setLastName(apellido.length() > 100 ? apellido.substring(0, 100) : apellido);
        user.setEmail(email);
        user.setRole(roleRepository.findById(1L).orElseThrow(() -> new ResourceNotFoundException("Role user not found")));
        user = userRepository.save(user);

        Account account = new Account();
        account.setUser(user);
        // Contraseña aleatoria imposible de adivinar: la cuenta se usa con Google
        account.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID() + java.util.UUID.randomUUID().toString()));
        account.setStatus("active");
        accountRepository.save(account);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        userProfileRepository.save(profile);
        return user;
    }

    public Map<String, Object> refreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtKey)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();

            String type = claims.get("type", String.class);
            if (!"refresh".equals(type)) {
                throw new InvalidCredentialsException("Invalid token type");
            }

            Long userId = Long.parseLong(claims.getSubject());
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            // Una cuenta bloqueada no puede seguir renovando su sesión
            Account account = accountRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
            if ("blocked".equals(account.getStatus()) || "inactive".equals(account.getStatus())) {
                throw new InvalidCredentialsException("Account blocked");
            }

            String role = user.getRole() != null && user.getRole().getId() == 2L ? "admin" : "user";
            String newAccessToken = generateToken(user.getId(), role);

            Map<String, Object> response = new HashMap<>();
            response.put("token", newAccessToken);
            return response;

        } catch (Exception e) {
            throw new InvalidCredentialsException("Refresh token inválido o expirado");
        }
    }

    public String generateTokenForUser(User user) {
        String role = user.getRole() != null && user.getRole().getId() == 2L ? "admin" : "user";
        return generateToken(user.getId(), role);
    }

    /** Refresh token de 15 días para que la app renueve la sesión sin pedir la contraseña. */
    public String generateRefreshTokenForUser(User user) {
        return generateRefreshToken(user.getId());
    }

    /** Cambia la contraseña de la cuenta en sesión; exige la actual para evitar cambios desde una sesión ajena. */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("La cuenta no existe"));
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, account.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta");
        }
        if (newPassword == null || newPassword.length() < 8 || newPassword.length() > 100) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
        if (passwordEncoder.matches(newPassword, account.getPasswordHash())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser distinta de la actual");
        }
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
        if (esAdmin(account.getUser())) sesionAdmin(account.getId(), AdminSessionEvent.PASSWORD_CHANGED, null);
    }

    /** Cierre de sesión voluntario o por inactividad (el JWT no se revoca; queda constancia en la auditoría). */
    public void logout(Long userId, String motivo) {
        if (userId == null) return;
        userRepository.findById(userId).filter(this::esAdmin).ifPresent(u ->
                accountRepository.findByUserId(userId).ifPresent(a -> sesionAdmin(a.getId(), AdminSessionEvent.LOGOUT, "inactividad".equals(motivo) ? "Por inactividad" : "Voluntario")));
    }

    private void sesionAdmin(Long accountId, String accion, String detalle) {
        events.publishEvent(new AdminSessionEvent(accountId, accion, detalle));
    }

    private boolean esAdmin(User user) {
        return user != null && user.getRole() != null && Long.valueOf(2L).equals(user.getRole().getId());
    }

    private String generateToken(Long userId, String role) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // 24 horas
                .signWith(jwtKey)
                .compact();
    }

    private String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1296000000L)) // 15 días (15 * 24 * 60 * 60 * 1000 ms)
                .signWith(jwtKey)
                .compact();
    }
}