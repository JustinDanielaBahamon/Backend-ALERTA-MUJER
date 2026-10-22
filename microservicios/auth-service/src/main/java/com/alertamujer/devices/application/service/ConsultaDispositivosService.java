package com.alertamujer.devices.application.service;

import com.alertamujer.devices.application.port.in.ConsultarDispositivosUseCase;
import com.alertamujer.devices.application.port.out.DispositivoRepositorio;
import com.alertamujer.devices.application.port.out.IdentityPort;
import com.alertamujer.devices.domain.model.Device;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Tokens push y celular principal para los otros módulos y Alert Service. */
@Service
@Transactional(readOnly = true)
public class ConsultaDispositivosService implements ConsultarDispositivosUseCase {

    private final DispositivoRepositorio repository;
    private final IdentityPort identidad;

    public ConsultaDispositivosService(DispositivoRepositorio repository, IdentityPort identidad) {
        this.repository = repository;
        this.identidad = identidad;
    }

    @Override
    public List<String> tokensDelPerfil(Long perfil) {
        // Los dispositivos se guardan por cuenta: perfil -> usuaria -> cuenta
        return identidad.userOfProfile(perfil).flatMap(identidad::cuentaDe).map(this::tokens).orElse(List.of());
    }

    @Override
    public List<String> tokensDeUsuaria(Long userId) {
        return identidad.cuentaDe(userId).map(this::tokens).orElse(List.of());
    }

    @Override
    public List<String> tokensDeCelulares(Long userId) {
        return identidad.cuentaDe(userId).map(cuenta -> repository.deCuenta(cuenta).stream()
                        .filter(Device::esCelular)
                        .filter(Device::tieneTokenPush)
                        .map(Device::getFcmToken)
                        .toList())
                .orElse(List.of());
    }

    @Override
    public boolean esElCelularMasReciente(Long userId, Long deviceId) {
        if (deviceId == null) return true;
        return identidad.cuentaDe(userId)
                .map(cuenta -> Device.esElCelularMasReciente(repository.deCuenta(cuenta), deviceId))
                .orElse(true);
    }

    @Override
    public long contar() {
        return repository.contar();
    }

    private List<String> tokens(Long cuenta) {
        return repository.deCuenta(cuenta).stream().filter(Device::tieneTokenPush).map(Device::getFcmToken).toList();
    }
}
