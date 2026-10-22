package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.InformesUseCase;
import com.alertamujer.admin.application.port.out.AlertsPort;
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

    private final InformeRepositorio repository;
    private final AlertsPort alerts;
    private final UsuariasPort usuarias;
    private final ZonasPort zonas;
    private final ExportadorInformesPort exportador;

    public InformesService(InformeRepositorio repository, AlertsPort alerts, UsuariasPort usuarias, ZonasPort zonas,
                           ExportadorInformesPort exportador) {
        this.repository = repository;
        this.alerts = alerts;
        this.usuarias = usuarias;
        this.zonas = zonas;
        this.exportador = exportador;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertReportView> listAll() {
        List<AdminAlertView> todas = alerts.todas();
        Map<Long, UserSummary> nombres = usuarias.porId();
        return repository.todos().stream()
                .sorted(Comparator.comparing(AlertReport::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(r -> view(r, nombres.get(r.getCreatedByUserId()), alertasDe(r, todas).size()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AlertReportView obtener(Long id) {
        return view(existente(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ReportStats stats() {
        List<AlertReport> informes = repository.todos();
        long exportaciones = informes.stream().mapToLong(r -> r.getExportCount() == null ? 0 : r.getExportCount()).sum();
        return new ReportStats(informes.size(), alerts.todas().size(), zonas.activas(), exportaciones);
    }

    @Override
    @Transactional
    public AlertReportView create(CambiosInforme cambios, Long adminUserId) {
        return view(repository.save(AlertReport.nuevo(cambios, adminUserId)));
    }

    @Override
    @Transactional
    public AlertReportView actualizar(Long id, CambiosInforme cambios) {
        AlertReport r = existente(id);
        r.aplicar(cambios);
        r.setUpdatedAt(LocalDateTime.now());
        return view(repository.save(r));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(existente(id).getId());
    }

    @Override
    @Transactional
    public byte[] exportar(Long id, boolean pdf) {
        AlertReport r = existente(id);
        AlertReportView informe = view(r);
        List<AdminAlertView> incluidas = alertasDe(r, alerts.todas());
        byte[] file = pdf ? exportador.pdf(informe, incluidas) : exportador.csv(informe, incluidas);
        r.registrarExportacion();
        repository.save(r);
        return file;
    }

    /** Alertas que cubre el informe: las elegidas o, si no hay, las que cumplen sus filtros. */
    static List<AdminAlertView> alertasDe(AlertReport r, List<AdminAlertView> todas) {
        return todas.stream().filter(a -> r.incluye(a.id(), a.startedAt(), a.status())).toList();
    }

    private AlertReport existente(Long id) {
        return repository.find(id).orElseThrow(() -> new ResourceNotFoundException("AlertReport", id));
    }

    private AlertReportView view(AlertReport r) {
        UserSummary creadora = r.getCreatedByUserId() == null ? null : usuarias.find(r.getCreatedByUserId()).orElse(null);
        return view(r, creadora, alertasDe(r, alerts.todas()).size());
    }

    private static AlertReportView view(AlertReport r, UserSummary creadora, int cantidadAlertas) {
        return new AlertReportView(r.getId(), r.getName(), r.getCity(), r.getStatus(), r.getUserLabel(), r.getEvent(),
                r.getDescription(), r.getFormat(), r.idsElegidos().stream().sorted().toList(), r.getFromDate(), r.getToDate(),
                r.getAlertStatus(), cantidadAlertas, r.getExportCount(), creadora == null ? null : creadora.fullName(),
                r.getCreatedAt(), r.getUpdatedAt());
    }
}
