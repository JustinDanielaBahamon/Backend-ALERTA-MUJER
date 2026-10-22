package com.alertamujer.devices.application.service;

import com.alertamujer.devices.application.port.in.MisDispositivosUseCase;
import com.alertamujer.devices.application.port.out.DispositivoRepositorio;
import com.alertamujer.devices.application.port.out.EventosDispositivosPort;
import com.alertamujer.devices.application.port.out.IdentityPort;
import com.alertamujer.devices.domain.model.DatosDispositivo;
import com.alertamujer.devices.domain.model.Device;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Dispositivos de la persona en sesión. Solo conoce puertos. */
@Service
public class MisDispositivosService implements MisDispositivosUseCase {

    private final DispositivoRepositorio repository;
    private final IdentityPort identidad;
    private final EventosDispositivosPort events;

    public MisDispositivosService(DispositivoRepositorio repository, IdentityPort identidad, EventosDispositivosPort events) {
        this.repository = repository;
        this.identidad = identidad;
        this.events = events;
    }

    @Override
    public List<Device> mios(Long userId) {
        return repository.deCuenta(identidad.cuentaRequerida(userId));
    }

    @Override
    public Device uno(Long id, Long userId) {
        Device d = repository.find(id).orElseThrow(() -> new ResourceNotFoundException("Device", id));
        if (!d.esDe(identidad.cuentaRequerida(userId))) {
            throw new AccessDeniedException("No puedes acceder a dispositivos de otro usuario");
        }
        return d;
    }

    @Override
    @Transactional
    public Device registrar(DatosDispositivo data, Long userId) {
        Long cuenta = identidad.cuentaRequerida(userId);
        LocalDateTime ahora = LocalDateTime.now();
        // El mismo celular volviendo a registrarse (app reinstalada o datos borrados): se reutiliza el
        // registro en vez de duplicarlo, y no se avisa de "otro celular"
        Optional<Device> mismo = repository.deCuenta(cuenta).stream()
                .filter(d -> d.esMismoCelular(data))
                .max(Comparator.comparing(Device::getLastAccess, Comparator.nullsFirst(Comparator.naturalOrder())));
        if (mismo.isPresent()) {
            Device d = mismo.get();
            d.registrarUso(data, ahora);
            return repository.save(d);
        }
        Device stored = repository.save(Device.nuevo(cuenta, data, ahora));
        avisarNuevoCelular(stored);
        return stored;
    }

    @Override
    @Transactional
    public Device reemplazar(Long id, DatosDispositivo data, Long userId) {
        Device d = uno(id, userId);
        d.reemplazar(data);
        return repository.save(d);
    }

    @Override
    @Transactional
    public Device registrarUso(Long id, DatosDispositivo data, Long userId) {
        Device d = uno(id, userId);
        d.registrarUso(data, LocalDateTime.now());
        return repository.save(d);
    }

    @Override
    @Transactional
    public void cambiarTokenPush(Long id, String token, Long userId) {
        Device d = uno(id, userId);
        d.cambiarTokenPush(token);
        repository.save(d);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        repository.delete(uno(id, userId).getId());
    }

    /** Se entró desde otro celular: se avisa a los demás celulares de la usuaria por si no fue ella. */
    private void avisarNuevoCelular(Device nuevo) {
        if (!nuevo.esCelular()) return;
        List<String> tokens = repository.deCuenta(nuevo.getAccountId()).stream()
                .filter(d -> !d.getId().equals(nuevo.getId()))
                .filter(Device::esCelular)
                .filter(Device::tieneTokenPush)
                .map(Device::getFcmToken)
                .toList();
        if (!tokens.isEmpty()) events.nuevoCelular(nuevo.nombreParaAviso(), tokens);
    }
}
