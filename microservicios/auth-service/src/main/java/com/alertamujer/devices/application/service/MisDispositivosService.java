package com.alertamujer.devices.application.service;

import com.alertamujer.devices.application.port.in.MisDispositivosUseCase;
import com.alertamujer.devices.application.port.out.DispositivoRepositorio;
import com.alertamujer.devices.application.port.out.EventosDispositivosPort;
import com.alertamujer.devices.application.port.out.IdentidadPort;
import com.alertamujer.devices.domain.model.DatosDispositivo;
import com.alertamujer.devices.domain.model.Device;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Dispositivos de la persona en sesión. Solo conoce puertos. */
@Service
public class MisDispositivosService implements MisDispositivosUseCase {

    private final DispositivoRepositorio repositorio;
    private final IdentidadPort identidad;
    private final EventosDispositivosPort eventos;

    public MisDispositivosService(DispositivoRepositorio repositorio, IdentidadPort identidad, EventosDispositivosPort eventos) {
        this.repositorio = repositorio;
        this.identidad = identidad;
        this.eventos = eventos;
    }

    @Override
    public List<Device> mios(Long userId) {
        return repositorio.deCuenta(identidad.cuentaRequerida(userId));
    }

    @Override
    public Device uno(Long id, Long userId) {
        Device d = repositorio.buscar(id).orElseThrow(() -> new ResourceNotFoundException("Device", id));
        if (!d.esDe(identidad.cuentaRequerida(userId))) {
            throw new AccessDeniedException("No puedes acceder a dispositivos de otro usuario");
        }
        return d;
    }

    @Override
    @Transactional
    public Device registrar(DatosDispositivo datos, Long userId) {
        Device guardado = repositorio.guardar(Device.nuevo(identidad.cuentaRequerida(userId), datos, LocalDateTime.now()));
        avisarNuevoCelular(guardado);
        return guardado;
    }

    @Override
    @Transactional
    public Device reemplazar(Long id, DatosDispositivo datos, Long userId) {
        Device d = uno(id, userId);
        d.reemplazar(datos);
        return repositorio.guardar(d);
    }

    @Override
    @Transactional
    public Device registrarUso(Long id, DatosDispositivo datos, Long userId) {
        Device d = uno(id, userId);
        d.registrarUso(datos, LocalDateTime.now());
        return repositorio.guardar(d);
    }

    @Override
    @Transactional
    public void cambiarTokenPush(Long id, String token, Long userId) {
        Device d = uno(id, userId);
        d.cambiarTokenPush(token);
        repositorio.guardar(d);
    }

    @Override
    @Transactional
    public void eliminar(Long id, Long userId) {
        repositorio.eliminar(uno(id, userId).getId());
    }

    /** Se entró desde otro celular: se avisa a los demás celulares de la usuaria por si no fue ella. */
    private void avisarNuevoCelular(Device nuevo) {
        if (!nuevo.esCelular()) return;
        List<String> tokens = repositorio.deCuenta(nuevo.getAccountId()).stream()
                .filter(d -> !d.getId().equals(nuevo.getId()))
                .filter(Device::esCelular)
                .filter(Device::tieneTokenPush)
                .map(Device::getFcmToken)
                .toList();
        if (!tokens.isEmpty()) eventos.nuevoCelular(nuevo.nombreParaAviso(), tokens);
    }
}
