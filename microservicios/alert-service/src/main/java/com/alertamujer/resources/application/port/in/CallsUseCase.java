package com.alertamujer.resources.application.port.in;

import com.alertamujer.resources.domain.model.ResourceCall;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Registro de las llamadas de la usuaria a los recursos de ayuda. */
public interface CallsUseCase {

    List<ResourceCall> all();

    /** Vacío si no existe o es de otra usuaria. */
    Optional<ResourceCall> one(Long id, Long userId);

    List<ResourceCall> mias(Long userId);

    List<ResourceCall> ofResource(Long resourceId);

    List<ResourceCall> ofAlert(Long alertId);

    ResourceCall registrar(Long resourceId, Long alertId, Long deviceId, String dialedNumber, String status, Long userId);

    ResourceCall update(Long id, String status, LocalDateTime end, Integer durationSeconds, Long userId);

    void delete(Long id, Long userId);
}
