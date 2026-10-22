package com.alertamujer.zones.application.service;

import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import com.alertamujer.zones.application.port.in.ReportesZonaUseCase;
import com.alertamujer.zones.application.port.out.IdentityPort;
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

    private final ReporteZonaRepositorio repository;
    private final ZonaRepositorio zonas;
    private final IdentityPort identidad;

    public ReportesZonaService(ReporteZonaRepositorio repository, ZonaRepositorio zonas, IdentityPort identidad) {
        this.repository = repository;
        this.zonas = zonas;
        this.identidad = identidad;
    }

    @Override
    public List<ZoneReport> todos() {
        return repository.todos();
    }

    @Override
    public Optional<ZoneReport> uno(Long id, Long userId) {
        Long perfil = identidad.profileOf(userId).orElse(null);
        return repository.find(id).filter(r -> r.esDe(perfil));
    }

    @Override
    public List<ZoneReport> deZona(Long zoneId) {
        return repository.deZona(zoneId);
    }

    @Override
    public List<ZoneReport> deUsuaria(Long idPedido, Long userId, boolean admin) {
        Long propio = identidad.perfilRequerido(userId);
        if (idPedido.equals(userId) || idPedido.equals(propio)) return repository.dePerfil(propio);
        if (!admin) throw new AccessDeniedException("No puedes ver los reportes de otra usuaria");
        return repository.dePerfil(identidad.profileOf(idPedido).orElse(idPedido));
    }

    @Override
    public List<ZoneReport> conEstado(String status) {
        return repository.conEstado(status);
    }

    @Override
    @Transactional
    public ZoneReport create(Long zoneId, String clasificacion, String comentario, Long userId) {
        if (zoneId == null || zonas.find(zoneId).isEmpty()) throw new ResourceNotFoundException("Zone", zoneId);
        return repository.save(ZoneReport.nuevo(identidad.perfilRequerido(userId), zoneId, clasificacion, comentario));
    }

    @Override
    @Transactional
    public ZoneReport aprobar(Long id, Integer adminId) {
        ZoneReport r = existente(id);
        r.aprobar(adminId, LocalDateTime.now());
        return repository.save(r);
    }

    @Override
    @Transactional
    public ZoneReport rechazar(Long id, Integer adminId) {
        ZoneReport r = existente(id);
        r.rechazar(adminId, LocalDateTime.now());
        return repository.save(r);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        ZoneReport r = existente(id);
        if (!r.esDe(identidad.perfilRequerido(userId))) {
            throw new AccessDeniedException("No puedes eliminar reportes de otro usuario");
        }
        repository.delete(id);
    }

    private ZoneReport existente(Long id) {
        return repository.find(id).orElseThrow(() -> new ResourceNotFoundException("ZoneReport", id));
    }
}
