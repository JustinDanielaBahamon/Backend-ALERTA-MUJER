package com.alertamujer.identity.application.service;

import com.alertamujer.identity.api.AdminSessionEvent;
import com.alertamujer.identity.api.PasswordRecoveryRequestedEvent;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.RecoveryRequest;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.infrastructure.repository.AccountRepository;
import com.alertamujer.identity.infrastructure.repository.RecoveryRequestRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Recuperación de contraseña con un código de 6 dígitos enviado al correo (web y app móvil).
 *
 * - La respuesta al pedir el código es la misma exista o no la cuenta (no revela qué correos están registrados).
 * - El código vale 15 minutos y admite 5 intentos; pedir uno nuevo anula el anterior.
 * - Solo se guarda el hash del código.
 */
@Service
public class PasswordRecoveryService {

    static final int MINUTOS_VALIDEZ = 15;
    static final int MAX_INTENTOS = 5;
    static final int SEGUNDOS_ENTRE_SOLICITUDES = 60;
    static final String CODIGO_INVALIDO = "El código no es válido o ya venció. Pide uno nuevo.";

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final RecoveryRequestRepository recoveryRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final ApplicationEventPublisher events;
    private final SecureRandom random = new SecureRandom();

    public PasswordRecoveryService(UserRepository userRepository, AccountRepository accountRepository,
                                   RecoveryRequestRepository recoveryRepository, ApplicationEventPublisher events) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.recoveryRepository = recoveryRepository;
        this.events = events;
    }

    @Transactional
    public void solicitar(String email, String ip) {
        Optional<User> user = userRepository.findByEmail(normalizar(email));
        Optional<Account> account = user.flatMap(u -> accountRepository.findByUserId(u.getId()));
        if (account.isEmpty() || "blocked".equals(account.get().getStatus())) return;

        Long accountId = account.get().getId();
        LocalDateTime ahora = LocalDateTime.now();
        var pendientes = recoveryRepository.findByAccountIdAndStatus(accountId, "pending");
        // Evita que se usen los correos para molestar: un código por minuto
        boolean reciente = pendientes.stream().anyMatch(r -> r.getCreatedAt() != null
                && r.getCreatedAt().isAfter(ahora.minusSeconds(SEGUNDOS_ENTRE_SOLICITUDES)));
        if (reciente) return;
        pendientes.forEach(r -> r.setStatus("replaced"));
        recoveryRepository.saveAll(pendientes);

        String codigo;
        String hash;
        do {
            codigo = String.format("%06d", random.nextInt(1_000_000));
            hash = hash(accountId, codigo);
        } while (recoveryRepository.findByTokenHash(hash).isPresent());

        RecoveryRequest solicitud = new RecoveryRequest();
        solicitud.setAccountId(accountId);
        solicitud.setMethod("email");
        solicitud.setTokenHash(hash);
        solicitud.setExpiresAt(ahora.plusMinutes(MINUTOS_VALIDEZ));
        solicitud.setRequestedIp(ip);
        recoveryRepository.save(solicitud);

        User u = user.get();
        String nombre = ((u.getFirstName() == null ? "" : u.getFirstName()) + " "
                + (u.getLastName() == null ? "" : u.getLastName())).trim();
        events.publishEvent(new PasswordRecoveryRequestedEvent(u.getEmail(), nombre, codigo, MINUTOS_VALIDEZ));
    }

    /** Comprueba el código sin usarlo (paso intermedio de las pantallas). Cuenta como intento. */
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void verificar(String email, String codigo) {
        validar(email, codigo);
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void restablecer(String email, String codigo, String nuevaContrasena) {
        if (nuevaContrasena == null || nuevaContrasena.length() < 8 || nuevaContrasena.length() > 100) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
        RecoveryRequest solicitud = validar(email, codigo);
        Account account = accountRepository.findById(solicitud.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException(CODIGO_INVALIDO));
        account.setPasswordHash(passwordEncoder.encode(nuevaContrasena));
        accountRepository.save(account);

        solicitud.setStatus("used");
        solicitud.setUsedAt(LocalDateTime.now());
        recoveryRepository.save(solicitud);
        User user = account.getUser();
        if (user != null && user.getRole() != null && Long.valueOf(2L).equals(user.getRole().getId())) {
            events.publishEvent(new AdminSessionEvent(account.getId(), AdminSessionEvent.PASSWORD_CHANGED, "Recuperación por correo"));
        }
    }

    private RecoveryRequest validar(String email, String codigo) {
        if (codigo == null || !codigo.matches("\\d{6}")) throw new IllegalArgumentException(CODIGO_INVALIDO);
        Account account = userRepository.findByEmail(normalizar(email))
                .flatMap(u -> accountRepository.findByUserId(u.getId()))
                .filter(a -> !"blocked".equals(a.getStatus()))
                .orElseThrow(() -> new IllegalArgumentException(CODIGO_INVALIDO));

        RecoveryRequest solicitud = recoveryRepository.findByAccountIdAndStatus(account.getId(), "pending").stream()
                .max(Comparator.comparing(RecoveryRequest::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElseThrow(() -> new IllegalArgumentException(CODIGO_INVALIDO));

        if (solicitud.getExpiresAt().isBefore(LocalDateTime.now())) {
            solicitud.setStatus("expired");
            recoveryRepository.save(solicitud);
            throw new IllegalArgumentException(CODIGO_INVALIDO);
        }
        if (!MessageDigest.isEqual(hash(account.getId(), codigo).getBytes(StandardCharsets.UTF_8),
                solicitud.getTokenHash().getBytes(StandardCharsets.UTF_8))) {
            solicitud.setAttempts(solicitud.getAttempts() + 1);
            if (solicitud.getAttempts() >= MAX_INTENTOS) solicitud.setStatus("expired");
            recoveryRepository.save(solicitud);
            throw new IllegalArgumentException(CODIGO_INVALIDO);
        }
        return solicitud;
    }

    static String hash(Long accountId, String codigo) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256")
                    .digest((accountId + ":" + codigo).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String normalizar(String email) {
        return email == null ? "" : email.trim();
    }
}
