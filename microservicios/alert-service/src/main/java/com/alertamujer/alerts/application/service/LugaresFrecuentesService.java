package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.LugaresFrecuentesUseCase;
import com.alertamujer.alerts.application.port.out.IdentityPort;
import com.alertamujer.alerts.application.port.out.LugarFrecuenteRepositorio;
import com.alertamujer.alerts.domain.model.DatosLugar;
import com.alertamujer.alerts.domain.model.FrequentLocation;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Lugares frecuentes de la usuaria (casa, trabajo...). */
@Service
public class LugaresFrecuentesService implements LugaresFrecuentesUseCase {

    private final LugarFrecuenteRepositorio repository;
    private final IdentityPort identidad;

    public LugaresFrecuentesService(LugarFrecuenteRepositorio repository, IdentityPort identidad) {
        this.repository = repository;
        this.identidad = identidad;
    }

    @Override
    public List<FrequentLocation> activosDe(Long userId) {
        return repository.activosDeLaUsuaria(identidad.perfilRequerido(userId));
    }

    @Override
    public FrequentLocation uno(Long id, Long userId) {
        FrequentLocation lugar = repository.find(id).orElseThrow(() -> new ResourceNotFoundException("FrequentLocation", id));
        if (!lugar.esDe(identidad.perfilRequerido(userId))) {
            throw new AccessDeniedException("No puedes acceder a ubicaciones frecuentes de otro usuario");
        }
        return lugar;
    }

    @Override
    @Transactional
    public FrequentLocation create(DatosLugar data, Long userId) {
        // La dueña sale del JWT; se ignora cualquier perfil del body (evita IDOR)
        return repository.save(FrequentLocation.nuevo(identidad.perfilRequerido(userId), data, LocalDateTime.now()));
    }

    @Override
    @Transactional
    public FrequentLocation actualizar(Long id, DatosLugar data, Long userId) {
        FrequentLocation lugar = uno(id, userId);
        lugar.actualizar(data, LocalDateTime.now());
        return repository.save(lugar);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        repository.delete(uno(id, userId).getId());
    }
}
