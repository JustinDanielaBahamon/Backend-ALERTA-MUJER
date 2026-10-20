package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.AuditoriaUseCase;
import com.alertamujer.admin.application.port.out.AuditoriaRepositorio;
import com.alertamujer.admin.application.port.out.UsuariasPort;
import com.alertamujer.admin.domain.model.AuditLog;
import com.alertamujer.admin.domain.model.FiltroAuditoria;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.web.HoraColombia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Auditoría del panel: lista, responsables y exportación a CSV. */
@Service
public class AuditoriaService implements AuditoriaUseCase {

    static final int TAMANO_MAXIMO = 100;
    static final int MAXIMO_EXPORTACION = 5000;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final AuditoriaRepositorio repositorio;
    private final UsuariasPort usuarias;

    public AuditoriaService(AuditoriaRepositorio repositorio, UsuariasPort usuarias) {
        this.repositorio = repositorio;
        this.usuarias = usuarias;
    }

    @Override
    @Transactional(readOnly = true)
    public AuditPage listar(FiltroAuditoria filtro, int pagina, int tamano) {
        int size = Math.max(1, Math.min(tamano, TAMANO_MAXIMO));
        AuditoriaRepositorio.Pagina p = repositorio.pagina(filtro, Math.max(0, pagina), size);
        return new AuditPage(vistas(p.registros()), p.total(), p.numero(), size);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportar(FiltroAuditoria filtro) {
        StringBuilder sb = new StringBuilder("﻿");
        sb.append("ID;Fecha;Responsable;Rol;Correo;Origen;Acción;Tipo;Registro;Detalle;IP\r\n");
        for (AuditLogView v : vistas(repositorio.primeros(filtro, MAXIMO_EXPORTACION))) {
            sb.append(String.join(";", List.of(
                    String.valueOf(v.id()),
                    v.createdAt() == null ? "" : FECHA.format(HoraColombia.de(v.createdAt())),
                    celda(v.actorName() == null ? "Sin responsable" : v.actorName()),
                    celda(v.actorRole()),
                    celda(v.actorEmail()),
                    celda(v.source()),
                    celda(v.action()),
                    celda(v.entityType()),
                    v.entityId() == null ? "" : String.valueOf(v.entityId()),
                    celda(v.description()),
                    celda(v.ipAddress())))).append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditActor> responsables() {
        return usuarias.porCuenta().entrySet().stream()
                .filter(e -> e.getValue().admin())
                .map(e -> new AuditActor(e.getKey(), e.getValue().fullName(), e.getValue().email()))
                .sorted(Comparator.comparing(AuditActor::name, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    private List<AuditLogView> vistas(List<AuditLog> registros) {
        Map<Long, UserSummary> porCuenta = usuarias.porCuenta();
        return registros.stream().map(r -> {
            UserSummary u = r.getAccountId() == null ? null : porCuenta.get(r.getAccountId());
            return new AuditLogView(r.getId(), r.getAction(), r.getEntityType(), r.getEntityId(), r.getDescription(),
                    r.getAccountId(), u == null ? null : u.fullName(), u == null ? null : u.email(), rol(u), r.getSource(),
                    r.getIpAddress(), r.getCreatedAt());
        }).toList();
    }

    private static String rol(UserSummary u) {
        if (u == null || u.roleId() == null) return null;
        return u.admin() ? "Administración" : "Usuaria";
    }

    /** Entre comillas y protegida contra fórmulas (inyección CSV). */
    static String celda(String valor) {
        String v = valor == null ? "" : valor;
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) v = "'" + v;
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}
