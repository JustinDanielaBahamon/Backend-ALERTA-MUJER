package com.alertamujer.admin.application.service;

import com.alertamujer.admin.domain.model.AuditLog;
import com.alertamujer.admin.infrastructure.repository.AuditLogRepository;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.infrastructure.repository.AccountRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Historial de cambios que registran los triggers de auditoría, con el nombre de quien actuó. */
@Service
public class AuditLogService {

    static final int LIMITE_MAXIMO = 500;

    private final AuditLogRepository auditLogRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, AccountRepository accountRepository,
                           UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    /** Los más recientes primero; entityType (p. ej. ALERT) filtra por tipo de registro. */
    @Transactional(readOnly = true)
    public List<AuditLogView> listar(String entityType, int limite) {
        PageRequest pagina = PageRequest.of(0, Math.max(1, Math.min(limite, LIMITE_MAXIMO)), Sort.by(Sort.Direction.DESC, "id"));
        List<AuditLog> registros = (entityType == null || entityType.isBlank()
                ? auditLogRepository.findAll(pagina)
                : auditLogRepository.findByEntityType(entityType.trim().toUpperCase(), pagina)).getContent();

        List<Long> cuentasIds = registros.stream().map(AuditLog::getAccountId).filter(Objects::nonNull).distinct().toList();
        Map<Long, Long> usuarioPorCuenta = accountRepository.findAllById(cuentasIds).stream()
                .collect(Collectors.toMap(Account::getId, Account::getUserId, (a, b) -> a));
        Map<Long, User> usuarios = userRepository.findAllById(usuarioPorCuenta.values()).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return registros.stream().map(r -> {
            User u = r.getAccountId() == null ? null : usuarios.get(usuarioPorCuenta.get(r.getAccountId()));
            String nombre = u == null ? null : ((u.getFirstName() == null ? "" : u.getFirstName()) + " "
                    + (u.getLastName() == null ? "" : u.getLastName())).trim();
            return new AuditLogView(r.getId(), r.getAction(), r.getEntityType(), r.getEntityId(), r.getDescription(),
                    nombre, u == null ? null : u.getEmail(), r.getIpAddress(), r.getCreatedAt());
        }).toList();
    }

    /** actorName es null cuando el cambio lo hizo el sistema (p. ej. una tarea programada). */
    public record AuditLogView(Long id, String action, String entityType, Integer entityId, String description,
                               String actorName, String actorEmail, String ipAddress, LocalDateTime createdAt) {}
}
