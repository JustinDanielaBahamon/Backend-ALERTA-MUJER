package com.alertamujer.resources.application.port.in;

import com.alertamujer.resources.domain.model.ResourceCall;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Registro de las llamadas de la usuaria a los recursos de ayuda. */
public interface LlamadasUseCase {

    List<ResourceCall> todas();

    /** Vacío si no existe o es de otra usuaria. */
    Optional<ResourceCall> una(Long id, Long userId);

    List<ResourceCall> mias(Long userId);

    List<ResourceCall> deRecurso(Long recursoId);

    List<ResourceCall> deAlerta(Long alertId);

    ResourceCall registrar(Long recursoId, Long alertId, Long deviceId, String telefonoMarcado, String estado, Long userId);

    ResourceCall actualizar(Long id, String estado, LocalDateTime fin, Integer duracionSegundos, Long userId);

    void eliminar(Long id, Long userId);
}
