package com.alertamujer.alerts.application.service;

import com.alertamujer.alerts.application.port.in.LugaresFrecuentesUseCase;
import com.alertamujer.alerts.application.port.out.IdentidadPort;
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

    private final LugarFrecuenteRepositorio repositorio;
    private final IdentidadPort identidad;

    public LugaresFrecuentesService(LugarFrecuenteRepositorio repositorio, IdentidadPort identidad) {
        this.repositorio = repositorio;
        this.identidad = identidad;
    }

    @Override
    public List<FrequentLocation> todos() {
        return repositorio.todos();
    }

    @Override
    public List<FrequentLocation> activosDe(Long userId) {
        return repositorio.activosDeLaUsuaria(identidad.perfilRequerido(userId));
    }

    @Override
    public FrequentLocation uno(Long id, Long userId) {
        FrequentLocation lugar = repositorio.buscar(id).orElseThrow(() -> new ResourceNotFoundException("FrequentLocation", id));
        if (!lugar.esDe(identidad.perfilRequerido(userId))) {
            throw new AccessDeniedException("No puedes acceder a ubicaciones frecuentes de otro usuario");
        }
        return lugar;
    }

    @Override
    @Transactional
    public FrequentLocation crear(DatosLugar datos, Long userId) {
        // La dueña sale del JWT; se ignora cualquier perfil del body (evita IDOR)
        return repositorio.guardar(FrequentLocation.nuevo(identidad.perfilRequerido(userId), datos, LocalDateTime.now()));
    }

    @Override
    @Transactional
    public FrequentLocation actualizar(Long id, DatosLugar datos, Long userId) {
        FrequentLocation lugar = uno(id, userId);
        lugar.actualizar(datos, LocalDateTime.now());
        return repositorio.guardar(lugar);
    }

    @Override
    @Transactional
    public void eliminar(Long id, Long userId) {
        repositorio.eliminar(uno(id, userId).getId());
    }
}
