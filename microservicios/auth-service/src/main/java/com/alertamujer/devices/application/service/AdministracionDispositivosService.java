package com.alertamujer.devices.application.service;

import com.alertamujer.devices.application.port.in.AdministrarDispositivosUseCase;
import com.alertamujer.devices.application.port.out.DispositivoRepositorio;
import com.alertamujer.devices.application.port.out.IdentidadPort;
import com.alertamujer.devices.domain.model.Device;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Dispositivos para el panel: datos del celular más la dueña (el dispositivo solo guarda la cuenta). */
@Service
public class AdministracionDispositivosService implements AdministrarDispositivosUseCase {

    private final DispositivoRepositorio repositorio;
    private final IdentidadPort identidad;

    public AdministracionDispositivosService(DispositivoRepositorio repositorio, IdentidadPort identidad) {
        this.repositorio = repositorio;
        this.identidad = identidad;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DispositivoConDuena> listar() {
        Map<Long, UserSummary> duenas = identidad.duenasPorCuenta();
        return repositorio.todos().stream()
                .sorted(Comparator.comparing(Device::getLastAccess, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(d -> new DispositivoConDuena(d, duenas.get(d.getAccountId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DispositivoConDuena obtener(Long id) {
        Device d = existente(id);
        return new DispositivoConDuena(d, identidad.duenaDeCuenta(d.getAccountId()).orElse(null));
    }

    @Override
    @Transactional
    public DispositivoConDuena cambiarEstado(Long id, String estado) {
        if (estado == null || !Device.ESTADOS.contains(estado)) throw new IllegalArgumentException("Estado no válido: " + estado);
        Device d = existente(id);
        d.cambiarEstado(estado);
        repositorio.guardar(d);
        return obtener(id);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        repositorio.eliminar(existente(id).getId());
    }

    private Device existente(Long id) {
        return repositorio.buscar(id).orElseThrow(() -> new ResourceNotFoundException("Device", id));
    }
}
