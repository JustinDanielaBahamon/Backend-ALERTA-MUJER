package com.alertamujer.admin.application.service;

import com.alertamujer.admin.domain.model.AlertReport;
import com.alertamujer.admin.infrastructure.repository.AlertReportRepository;
import com.alertamujer.alerts.application.service.AdminAlertService;
import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.infrastructure.repository.UserRepository;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import com.alertamujer.zones.infrastructure.repository.ZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Informes de alertas del panel de administración: se guardan con un nombre y las alertas
 * que incluyen (elegidas a mano o por filtros) y se exportan a PDF o Excel.
 */
@Service
public class AlertReportService {

    private static final Set<String> ESTADOS = Set.of("pending", "attended", "closed");
    private static final Set<String> FORMATOS = Set.of("PDF", "EXCEL");
    private static final Set<String> ESTADOS_ALERTA = Set.of("active", "resolved", "cancelled", "expired");

    private final AlertReportRepository alertReportRepository;
    private final AdminAlertService adminAlertService;
    private final UserRepository userRepository;
    private final ZoneRepository zoneRepository;

    public AlertReportService(AlertReportRepository alertReportRepository, AdminAlertService adminAlertService,
                              UserRepository userRepository, ZoneRepository zoneRepository) {
        this.alertReportRepository = alertReportRepository;
        this.adminAlertService = adminAlertService;
        this.userRepository = userRepository;
        this.zoneRepository = zoneRepository;
    }

    @Transactional(readOnly = true)
    public List<AlertReportView> listar() {
        List<AdminAlertView> alertas = adminAlertService.listar();
        Map<Long, User> usuarios = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return alertReportRepository.findAll().stream()
                .sorted(Comparator.comparing(AlertReport::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(r -> vista(r, usuarios.get(r.getCreatedByUserId()), alertasDe(r, alertas).size()))
                .toList();
    }

    @Transactional(readOnly = true)
    public AlertReportView obtener(Long id) {
        return vista(buscar(id));
    }

    @Transactional(readOnly = true)
    public ReportStats estadisticas() {
        List<AlertReport> informes = alertReportRepository.findAll();
        long exportaciones = informes.stream().mapToLong(r -> r.getExportCount() == null ? 0 : r.getExportCount()).sum();
        long zonasActivas = zoneRepository.findAll().stream().filter(z -> Boolean.TRUE.equals(z.getIsActive())).count();
        return new ReportStats(informes.size(), adminAlertService.listar().size(), zonasActivas, exportaciones);
    }

    @Transactional
    public AlertReportView crear(AlertReportRequest req, Long adminUserId) {
        if (req.name() == null || req.name().isBlank()) throw new IllegalArgumentException("El nombre del informe es obligatorio");
        AlertReport r = new AlertReport();
        aplicar(r, req);
        r.setCreatedByUserId(adminUserId);
        return vista(alertReportRepository.save(r));
    }

    @Transactional
    public AlertReportView actualizar(Long id, AlertReportRequest req) {
        AlertReport r = buscar(id);
        aplicar(r, req);
        r.setUpdatedAt(LocalDateTime.now());
        return vista(alertReportRepository.save(r));
    }

    @Transactional
    public void eliminar(Long id) {
        alertReportRepository.delete(buscar(id));
    }

    /** Alertas que cubre el informe: las elegidas o, si no hay, las que cumplen sus filtros. */
    @Transactional(readOnly = true)
    public List<AdminAlertView> alertasDe(Long id) {
        return alertasDe(buscar(id), adminAlertService.listar());
    }

    /** Suma una exportación al contador del informe. */
    @Transactional
    public void registrarExportacion(Long id) {
        AlertReport r = buscar(id);
        r.setExportCount((r.getExportCount() == null ? 0 : r.getExportCount()) + 1);
        alertReportRepository.save(r);
    }

    AlertReport buscar(Long id) {
        return alertReportRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("AlertReport", id));
    }

    static List<AdminAlertView> alertasDe(AlertReport r, List<AdminAlertView> todas) {
        Set<Long> ids = idsDe(r.getAlertIds());
        if (!ids.isEmpty()) {
            return todas.stream().filter(a -> ids.contains(a.id())).toList();
        }
        return todas.stream()
                .filter(a -> r.getFromDate() == null || (a.startedAt() != null && !a.startedAt().isBefore(r.getFromDate())))
                .filter(a -> r.getToDate() == null || (a.startedAt() != null && !a.startedAt().isAfter(r.getToDate())))
                .filter(a -> r.getAlertStatus() == null || r.getAlertStatus().equals(a.status()))
                .toList();
    }

    private static Set<Long> idsDe(String texto) {
        if (texto == null || texto.isBlank()) return Set.of();
        return Arrays.stream(texto.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                .map(Long::valueOf).collect(Collectors.toSet());
    }

    private void aplicar(AlertReport r, AlertReportRequest req) {
        if (req.name() != null && !req.name().isBlank()) r.setName(req.name().trim());
        if (req.city() != null) r.setCity(req.city().isBlank() ? null : req.city().trim());
        if (req.status() != null) {
            if (!ESTADOS.contains(req.status())) throw new IllegalArgumentException("Estado no válido: " + req.status());
            r.setStatus(req.status());
        }
        if (req.format() != null) {
            if (!FORMATOS.contains(req.format())) throw new IllegalArgumentException("Formato no válido: " + req.format());
            r.setFormat(req.format());
        }
        if (req.userLabel() != null) r.setUserLabel(req.userLabel().trim());
        if (req.event() != null) r.setEvent(req.event().trim());
        if (req.description() != null) r.setDescription(req.description().trim());
        if (req.alertIds() != null) {
            r.setAlertIds(req.alertIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
        }
        if (req.fromDate() != null) r.setFromDate(req.fromDate());
        if (req.toDate() != null) r.setToDate(req.toDate());
        if (req.alertStatus() != null) {
            if (!req.alertStatus().isBlank() && !ESTADOS_ALERTA.contains(req.alertStatus())) {
                throw new IllegalArgumentException("Estado de alerta no válido: " + req.alertStatus());
            }
            r.setAlertStatus(req.alertStatus().isBlank() ? null : req.alertStatus());
        }
    }

    private AlertReportView vista(AlertReport r) {
        User creadora = r.getCreatedByUserId() == null ? null : userRepository.findById(r.getCreatedByUserId()).orElse(null);
        return vista(r, creadora, alertasDe(r, adminAlertService.listar()).size());
    }

    private AlertReportView vista(AlertReport r, User creadora, int cantidadAlertas) {
        String nombre = creadora == null ? null
                : ((creadora.getFirstName() == null ? "" : creadora.getFirstName()) + " "
                   + (creadora.getLastName() == null ? "" : creadora.getLastName())).trim();
        List<Long> ids = idsDe(r.getAlertIds()).stream().sorted().toList();
        return new AlertReportView(r.getId(), r.getName(), r.getCity(), r.getStatus(), r.getUserLabel(), r.getEvent(),
                r.getDescription(), r.getFormat(), ids, r.getFromDate(), r.getToDate(), r.getAlertStatus(),
                cantidadAlertas, r.getExportCount(), nombre, r.getCreatedAt(), r.getUpdatedAt());
    }

    public record AlertReportRequest(String name, String city, String status, String userLabel, String event,
                                     String description, String format, List<Long> alertIds,
                                     LocalDateTime fromDate, LocalDateTime toDate, String alertStatus) {}

    public record AlertReportView(Long id, String name, String city, String status, String userLabel, String event,
                                  String description, String format, List<Long> alertIds, LocalDateTime fromDate,
                                  LocalDateTime toDate, String alertStatus, int alertCount, Integer exportCount,
                                  String createdByName, LocalDateTime createdAt, LocalDateTime updatedAt) {}

    public record ReportStats(long totalReports, long totalAlerts, long activeZones, long exports) {}
}
