package com.alertamujer.devices.application.port.in;

import java.util.List;

/** Consultas que piden los otros módulos y Alert Service. */
public interface ConsultarDispositivosUseCase {

    /** Tokens push de la dueña del perfil (user_profile.id). */
    List<String> tokensDelPerfil(Long perfil);

    /** Tokens push de la persona (users.id); vacío si no tiene cuenta. */
    List<String> tokensDeUsuaria(Long userId);

    /** Solo celulares (sin navegadores web): para pedir la ubicación por GPS. */
    List<String> tokensDeCelulares(Long userId);

    boolean esElCelularMasReciente(Long userId, Long deviceId);

    long contar();
}
