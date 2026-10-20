package com.alertamujer.zones.application.service;

import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import com.alertamujer.zones.application.port.in.ReportesZonaUseCase;
import com.alertamujer.zones.application.port.out.IdentidadPort;
import com.alertamujer.zones.application.port.out.ReporteZonaRepositorio;
import com.alertamujer.zones.application.port.out.ZonaRepositorio;
import com.alertamujer.zones.domain.model.ZoneReport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Reportes de zonas: la autora sale del token (user_profile.id), nunca del body. */
@Service
public class ReportesZonaService implements ReportesZonaUseCase {

    private final ReporteZonaRepositorio repositorio;
    private final ZonaRepositorio zonas;
    private final IdentidadPort identidad;

    public ReportesZonaService(ReporteZonaRepositorio repositorio, ZonaRepositorio zonas, IdentidadPort identidad) {
        this.repositorio = repositorio;
        this.zonas = zonas;
        this.identidad = identidad;
    }

    @Override
    public List<ZoneReport> todos() {
        return repositorio.todos();
    }

    @Override
    public Optional<ZoneReport> uno(Long id, Long userId) {
        Long perfil = identidad.perfilDe(userId).orElse(null);
        return repositorio.buscar(id).filter(r -> r.esDe(perfil));
    }

    @Override
    public List<ZoneReport> deZona(Long zoneId) {
        return repositorio.deZona(zoneId);
    }

    @Override
    public List<ZoneReport> deUsuaria(Long idPedido, Long userId, boolean admin) {
        Long propio = identidad.perfilRequerido(userId);
        if (idPedido.equals(userId) || idPedido.equals(propio)) return repositorio.dePerfil(propio);
        if (!admin) throw new AccessDeniedException("No puedes ver los reportes de otra usuaria");
        return repositorio.dePerfil(identidad.perfilDe(idPedido).orElse(idPedido));
    }

    @Override
    public List<ZoneReport> conEstado(String estado) {
        return repositorio.conEstado(estado);
    }

    @Override
    @Transactional
    public ZoneReport crear(Long zoneId, String clasificacion, String comentario, Long userId) {
        if (zoneId == null || zonas.buscar(zoneId).isEmpty()) throw new ResourceNotFoundException("Zone", zoneId);
        return repositorio.guardar(ZoneReport.nuevo(identidad.perfilRequerido(userId), zoneId, clasificacion, comentario));
    }

    @Override
    @Transactional
    public ZoneReport aprobar(Long id, Integer adminId) {
        ZoneReport r = existente(id);
        r.aprobar(adminId, LocalDateTime.now());
        return repositorio.guardar(r);
    }

    @Override
    @Transactional
    public ZoneReport rechazar(Long id, Integer adminId) {
        ZoneReport r = existente(id);
        r.rechazar(adminId, LocalDateTime.now());
        return repositorio.guardar(r);
    }

    @Override
    @Transactional
    public void eliminar(Long id, Long userId) {
        ZoneReport r = existente(id);
        if (!r.esDe(identidad.perfilRequerido(userId))) {
            throw new AccessDeniedException("No puedes eliminar reportes de otro usuario");
        }
        repositorio.eliminar(id);
    }

    private ZoneReport existente(Long id) {
        return repositorio.buscar(id).orElseThrow(() -> new ResourceNotFoundException("ZoneReport", id));
    }
}
