package com.alertamujer.settings.application.service;

import com.alertamujer.settings.application.port.in.AdministrarAjustesUseCase;
import com.alertamujer.settings.application.port.in.ConsultarAjustesUseCase;
import com.alertamujer.settings.application.port.out.ConfiguracionRepositorio;
import com.alertamujer.settings.domain.model.CambiosAjustes;
import com.alertamujer.settings.domain.model.SystemConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Parámetros globales de la app. Solo conoce el puerto de la configuración. */
@Service
public class AjustesService implements AdministrarAjustesUseCase, ConsultarAjustesUseCase {

    private final ConfiguracionRepositorio repositorio;

    public AjustesService(ConfiguracionRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    @Transactional(readOnly = true)
    public SystemConfiguration obtener() {
        return existente();
    }

    @Override
    @Transactional
    public SystemConfiguration actualizar(CambiosAjustes cambios, Long adminUserId) {
        SystemConfiguration c = existente();
        LocalDateTime ahora = LocalDateTime.now();
        c.actualizar(cambios, adminUserId, ahora);
        SystemConfiguration guardada = repositorio.guardar(c);
        guardada.setUpdatedAt(ahora);
        return guardada;
    }

    @Override
    @Transactional(readOnly = true)
    public SystemConfiguration publicos() {
        return existente();
    }

    @Override
    @Transactional(readOnly = true)
    public int duracionMaximaAlertaMinutos() {
        return repositorio.actual().map(SystemConfiguration::getSosMaxDurationMinutes).orElse(0);
    }

    @Override
    @Transactional(readOnly = true)
    public String numeroEmergencia() {
        return repositorio.actual().map(SystemConfiguration::getEmergencyPhoneNumber).orElse("123");
    }

    @Override
    @Transactional(readOnly = true)
    public int maximoContactos() {
        return repositorio.actual().map(SystemConfiguration::getMaxEmergencyContacts).orElse(5);
    }

    private SystemConfiguration existente() {
        return repositorio.actual().orElseThrow(() -> new IllegalStateException("Falta la configuración del sistema"));
    }
}
