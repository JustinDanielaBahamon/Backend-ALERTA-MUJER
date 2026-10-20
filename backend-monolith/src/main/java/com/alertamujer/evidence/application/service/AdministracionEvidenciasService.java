package com.alertamujer.evidence.application.service;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.evidence.application.port.in.AdministrarEvidenciasUseCase;
import com.alertamujer.evidence.application.port.out.AlertasPort;
import com.alertamujer.evidence.application.port.out.EvidenciaRepositorio;
import com.alertamujer.evidence.application.port.out.IdentidadPort;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Casos de uso del panel: evidencias con su alerta y su dueña, estadísticas y revisión. */
@Service
public class AdministracionEvidenciasService implements AdministrarEvidenciasUseCase {

    private final EvidenciaRepositorio repositorio;
    private final AlertasPort alertas;
    private final IdentidadPort identidad;

    public AdministracionEvidenciasService(EvidenciaRepositorio repositorio, AlertasPort alertas, IdentidadPort identidad) {
        this.repositorio = repositorio;
        this.alertas = alertas;
        this.identidad = identidad;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvidenciaAdmin> listar() {
        Map<Long, AlertSummary> porAlerta = alertas.todas();
        Map<Long, UserSummary> porPerfil = identidad.usuariasPorPerfil();
        return repositorio.todas().stream()
                .sorted(Comparator.comparing(Evidence::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(e -> {
                    AlertSummary a = porAlerta.get(e.getAlertId());
                    return new EvidenciaAdmin(e, a, a == null ? null : porPerfil.get(a.userProfileId()));
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Estadisticas estadisticas() {
        List<Evidence> todas = repositorio.todas();
        long verificadas = todas.stream().filter(e -> "verified".equals(e.getReviewStatus())).count();
        long rechazadas = todas.stream().filter(e -> "rejected".equals(e.getReviewStatus())).count();
        long tamanio = todas.stream().mapToLong(e -> e.getSizeBytes() == null ? 0 : e.getSizeBytes()).sum();
        return new Estadisticas(todas.size(), verificadas, todas.size() - verificadas - rechazadas, rechazadas, tamanio);
    }

    @Override
    @Transactional
    public EvidenciaAdmin cambiarRevision(Long id, String revision) {
        Evidence e = repositorio.buscar(id).orElseThrow(() -> new ResourceNotFoundException("Evidence", id));
        e.cambiarRevision(revision);
        e = repositorio.guardar(e);
        AlertSummary a = alertas.buscar(e.getAlertId()).orElse(null);
        UserSummary u = a == null ? null : identidad.usuariaDelPerfil(a.userProfileId()).orElse(null);
        return new EvidenciaAdmin(e, a, u);
    }
}
