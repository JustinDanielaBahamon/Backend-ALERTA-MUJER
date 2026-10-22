package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.AuditUseCase;
import com.alertamujer.admin.application.port.out.AuditRepository;
import com.alertamujer.admin.application.port.out.UsersPort;
import com.alertamujer.admin.domain.model.AuditLog;
import com.alertamujer.admin.domain.model.AuditFilter;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.web.ColombiaTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Auditoría del panel: lista, responsables y exportación a CSV. */
@Service
public class AuditService implements AuditUseCase {

    static final int MAX_SIZE = 100;
    static final int MAX_EXPORT = 5000;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final AuditRepository repository;
    private final UsersPort users;

    public AuditService(AuditRepository repository, UsersPort users) {
        this.repository = repository;
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public AuditPage listAll(AuditFilter filter, int page, int requestedSize) {
        int size = Math.max(1, Math.min(requestedSize, MAX_SIZE));
        AuditRepository.AuditLogPage p = repository.page(filter, Math.max(0, page), size);
        return new AuditPage(views(p.records()), p.total(), p.number(), size);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] export(AuditFilter filter) {
        StringBuilder sb = new StringBuilder("﻿");
        sb.append("ID;Fecha;Responsable;Rol;Correo;Origen;Acción;Tipo;Registro;Detalle;IP\r\n");
        for (AuditLogView v : views(repository.first(filter, MAX_EXPORT))) {
            sb.append(String.join(";", List.of(
                    String.valueOf(v.id()),
                    v.createdAt() == null ? "" : DATE.format(ColombiaTime.of(v.createdAt())),
                    cell(v.actorName() == null ? "Sin responsable" : v.actorName()),
                    cell(v.actorRole()),
                    cell(v.actorEmail()),
                    cell(v.source()),
                    cell(v.action()),
                    cell(v.entityType()),
                    v.entityId() == null ? "" : String.valueOf(v.entityId()),
                    cell(v.description()),
                    cell(v.ipAddress())))).append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditActor> actors() {
        return users.byAccount().entrySet().stream()
                .filter(e -> e.getValue().admin())
                .map(e -> new AuditActor(e.getKey(), e.getValue().fullName(), e.getValue().email()))
                .sorted(Comparator.comparing(AuditActor::name, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    private List<AuditLogView> views(List<AuditLog> records) {
        Map<Long, UserSummary> byAccount = users.byAccount();
        return records.stream().map(r -> {
            UserSummary u = r.getAccountId() == null ? null : byAccount.get(r.getAccountId());
            return new AuditLogView(r.getId(), r.getAction(), r.getEntityType(), r.getEntityId(), r.getDescription(),
                    r.getAccountId(), u == null ? null : u.fullName(), u == null ? null : u.email(), role(u), r.getSource(),
                    r.getIpAddress(), r.getCreatedAt());
        }).toList();
    }

    private static String role(UserSummary u) {
        if (u == null || u.roleId() == null) return null;
        return u.admin() ? "Administración" : "Usuaria";
    }

    /** Entre comillas y protegida contra fórmulas (inyección CSV). */
    static String cell(String value) {
        String v = value == null ? "" : value;
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) v = "'" + v;
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}
