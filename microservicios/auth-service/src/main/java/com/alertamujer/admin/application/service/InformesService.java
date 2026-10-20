package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.InformesUseCase;
import com.alertamujer.admin.application.port.out.AlertasPort;
import com.alertamujer.admin.application.port.out.ExportadorInformesPort;
import com.alertamujer.admin.application.port.out.InformeRepositorio;
import com.alertamujer.admin.application.port.out.UsuariasPort;
import com.alertamujer.admin.application.port.out.ZonasPort;
import com.alertamujer.admin.domain.model.AlertReport;
import com.alertamujer.admin.domain.model.CambiosInforme;
import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Informes de alertas: guardar, consultar con sus alertas y exportar. Solo conoce puertos. */
@Service
public class InformesService implements InformesUseCase {

    private final InformeRepositorio repositorio;
    private final AlertasPort alertas;
    private final UsuariasPort usuarias;
    private final ZonasPort zonas;
    private final ExportadorInformesPort exportador;

    public InformesService(InformeRepositorio repositorio, AlertasPort alertas, UsuariasPort usuarias, ZonasPort zonas,
                           ExportadorInformesPort exportador) {
        this.repositorio = repositorio;
        this.alertas = alertas;
        this.usuarias = usuarias;
        this.zonas = zonas;
        this.exportador = exportador;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertReportView> listar() {
        List<AdminAlertView> todas = alertas.todas();
        Map<Long, UserSummary> nombres = usuarias.porId();
        return repositorio.todos().stream()
                .sorted(Comparator.comparing(AlertReport::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(r -> vista(r, nombres.get(r.getCreatedByUserId()), alertasDe(r, todas).size()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AlertReportView obtener(Long id) {
        return vista(existente(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ReportStats estadisticas() {
        List<AlertReport> informes = repositorio.todos();
        long exportaciones = informes.stream().mapToLong(r -> r.getExportCount() == null ? 0 : r.getExportCount()).sum();
        return new ReportStats(informes.size(), alertas.todas().size(), zonas.activas(), exportaciones);
    }

    @Override
    @Transactional
    public AlertReportView crear(CambiosInforme cambios, Long adminUserId) {
        return vista(repositorio.guardar(AlertReport.nuevo(cambios, adminUserId)));
    }

    @Override
    @Transactional
    public AlertReportView actualizar(Long id, CambiosInforme cambios) {
        AlertReport r = existente(id);
        r.aplicar(cambios);
        r.setUpdatedAt(LocalDateTime.now());
        return vista(repositorio.guardar(r));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        repositorio.eliminar(existente(id).getId());
    }

    @Override
    @Transactional
    public byte[] exportar(Long id, boolean pdf) {
        AlertReport r = existente(id);
        AlertReportView informe = vista(r);
        List<AdminAlertView> incluidas = alertasDe(r, alertas.todas());
        byte[] archivo = pdf ? exportador.pdf(informe, incluidas) : exportador.csv(informe, incluidas);
        r.registrarExportacion();
        repositorio.guardar(r);
        return archivo;
    }

    /** Alertas que cubre el informe: las elegidas o, si no hay, las que cumplen sus filtros. */
    static List<AdminAlertView> alertasDe(AlertReport r, List<AdminAlertView> todas) {
        return todas.stream().filter(a -> r.incluye(a.id(), a.startedAt(), a.status())).toList();
    }

    private AlertReport existente(Long id) {
        return repositorio.buscar(id).orElseThrow(() -> new ResourceNotFoundException("AlertReport", id));
    }

    private AlertReportView vista(AlertReport r) {
        UserSummary creadora = r.getCreatedByUserId() == null ? null : usuarias.buscar(r.getCreatedByUserId()).orElse(null);
        return vista(r, creadora, alertasDe(r, alertas.todas()).size());
    }

    private static AlertReportView vista(AlertReport r, UserSummary creadora, int cantidadAlertas) {
        return new AlertReportView(r.getId(), r.getName(), r.getCity(), r.getStatus(), r.getUserLabel(), r.getEvent(),
                r.getDescription(), r.getFormat(), r.idsElegidos().stream().sorted().toList(), r.getFromDate(), r.getToDate(),
                r.getAlertStatus(), cantidadAlertas, r.getExportCount(), creadora == null ? null : creadora.fullName(),
                r.getCreatedAt(), r.getUpdatedAt());
    }
}
