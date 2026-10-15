package com.alertamujer.admin.application.service;

import com.alertamujer.admin.domain.model.AuditLog;
import com.alertamujer.admin.infrastructure.repository.AuditLogRepository;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.infrastructure.repository.AccountRepository;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Auditoría del panel: cambios registrados por los triggers y eventos de sesión de las cuentas admin. */
@Service
public class AuditLogService {

    static final int TAMANO_MAXIMO = 100;
    static final int MAXIMO_EXPORTACION = 5000;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final AuditLogRepository auditLogRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, AccountRepository accountRepository,
                           UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public AuditPage listar(AuditFilter filtro, int pagina, int tamano) {
        int size = Math.max(1, Math.min(tamano, TAMANO_MAXIMO));
        Page<AuditLog> resultado = auditLogRepository.findAll(especificacion(filtro),
                PageRequest.of(Math.max(0, pagina), size, Sort.by(Sort.Direction.DESC, "id")));
        return new AuditPage(vistas(resultado.getContent()), resultado.getTotalElements(), resultado.getNumber(), size);
    }

    /** CSV (';' y BOM UTF-8, para Excel en español) con los registros que cumplen el filtro. */
    @Transactional(readOnly = true)
    public byte[] exportar(AuditFilter filtro) {
        List<AuditLog> registros = auditLogRepository.findAll(especificacion(filtro),
                PageRequest.of(0, MAXIMO_EXPORTACION, Sort.by(Sort.Direction.DESC, "id"))).getContent();
        StringBuilder sb = new StringBuilder("﻿");
        sb.append("ID;Fecha;Responsable;Correo;Acción;Tipo;Registro;Detalle;IP\r\n");
        for (AuditLogView v : vistas(registros)) {
            sb.append(String.join(";", List.of(
                    String.valueOf(v.id()),
                    v.createdAt() == null ? "" : FECHA.format(v.createdAt()),
                    celda(v.actorName() == null ? "Sistema / app" : v.actorName()),
                    celda(v.actorEmail()),
                    celda(v.action()),
                    celda(v.entityType()),
                    v.entityId() == null ? "" : String.valueOf(v.entityId()),
                    celda(v.description()),
                    celda(v.ipAddress())))).append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /** Cuentas de administración, para filtrar por responsable. */
    @Transactional(readOnly = true)
    public List<AuditActor> responsables() {
        Map<Long, User> admins = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && Long.valueOf(2L).equals(u.getRole().getId()))
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return accountRepository.findAll().stream()
                .filter(a -> admins.containsKey(a.getUserId()))
                .map(a -> new AuditActor(a.getId(), nombre(admins.get(a.getUserId())), admins.get(a.getUserId()).getEmail()))
                .sorted(Comparator.comparing(AuditActor::name, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    static Specification<AuditLog> especificacion(AuditFilter f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (f.actorAccountId() != null) {
                p.add(f.actorAccountId() == 0 ? cb.isNull(root.get("accountId")) : cb.equal(root.get("accountId"), f.actorAccountId()));
            }
            if (f.entityType() != null && !f.entityType().isBlank()) p.add(cb.equal(root.get("entityType"), f.entityType().trim().toUpperCase()));
            if (f.action() != null && !f.action().isBlank()) p.add(cb.equal(root.get("action"), f.action().trim().toUpperCase()));
            if (f.from() != null) p.add(cb.greaterThanOrEqualTo(root.get("createdAt"), f.from().atStartOfDay()));
            if (f.to() != null) p.add(cb.lessThan(root.get("createdAt"), f.to().plusDays(1).atStartOfDay()));
            return cb.and(p.toArray(Predicate[]::new));
        };
    }

    private List<AuditLogView> vistas(List<AuditLog> registros) {
        List<Long> cuentasIds = registros.stream().map(AuditLog::getAccountId).filter(Objects::nonNull).distinct().toList();
        Map<Long, Long> usuarioPorCuenta = accountRepository.findAllById(cuentasIds).stream()
                .collect(Collectors.toMap(Account::getId, Account::getUserId, (a, b) -> a));
        Map<Long, User> usuarios = userRepository.findAllById(usuarioPorCuenta.values()).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return registros.stream().map(r -> {
            User u = r.getAccountId() == null ? null : usuarios.get(usuarioPorCuenta.get(r.getAccountId()));
            return new AuditLogView(r.getId(), r.getAction(), r.getEntityType(), r.getEntityId(), r.getDescription(),
                    r.getAccountId(), nombre(u), u == null ? null : u.getEmail(), r.getIpAddress(), r.getCreatedAt());
        }).toList();
    }

    private static String nombre(User u) {
        if (u == null) return null;
        return ((u.getFirstName() == null ? "" : u.getFirstName()) + " " + (u.getLastName() == null ? "" : u.getLastName())).trim();
    }

    private static String celda(String valor) {
        String v = valor == null ? "" : valor;
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) v = "'" + v;
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }

    /** actorAccountId = 0 filtra los cambios hechos por el sistema (sin responsable). */
    public record AuditFilter(Long actorAccountId, String entityType, String action, LocalDate from, LocalDate to) {}

    public record AuditPage(List<AuditLogView> items, long total, int page, int size) {}

    public record AuditActor(Long accountId, String name, String email) {}

    /** actorName es null cuando el cambio lo hizo el sistema (p. ej. una tarea programada). */
    public record AuditLogView(Long id, String action, String entityType, Integer entityId, String description,
                               Long actorAccountId, String actorName, String actorEmail, String ipAddress,
                               LocalDateTime createdAt) {}
}
