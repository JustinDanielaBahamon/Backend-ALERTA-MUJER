package com.alertamujer.devices.application.service;

import com.alertamujer.devices.application.port.in.AdministrarDispositivosUseCase;
import com.alertamujer.devices.application.port.out.DispositivoRepositorio;
import com.alertamujer.devices.application.port.out.IdentityPort;
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

    private final DispositivoRepositorio repository;
    private final IdentityPort identidad;

    public AdministracionDispositivosService(DispositivoRepositorio repository, IdentityPort identidad) {
        this.repository = repository;
        this.identidad = identidad;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DispositivoConDuena> listAll() {
        Map<Long, UserSummary> duenas = identidad.duenasPorCuenta();
        return repository.todos().stream()
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
        repository.save(d);
        return obtener(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(existente(id).getId());
    }

    private Device existente(Long id) {
        return repository.find(id).orElseThrow(() -> new ResourceNotFoundException("Device", id));
    }
}
