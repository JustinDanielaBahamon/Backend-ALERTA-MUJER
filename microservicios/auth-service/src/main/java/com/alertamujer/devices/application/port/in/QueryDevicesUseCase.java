package com.alertamujer.devices.application.port.in;

import java.util.List;

/** Consultas que piden los otros módulos y Alert Service. */
public interface QueryDevicesUseCase {

    /** Tokens push de la dueña del perfil (user_profile.id). */
    List<String> profileTokens(Long profile);

    /** Tokens push de la persona (users.id); vacío si no tiene cuenta. */
    List<String> userTokens(Long userId);

    /** Solo celulares (sin navegadores web): para pedir la ubicación por GPS. */
    List<String> phoneTokens(Long userId);

    boolean isTheNewestPhone(Long userId, Long deviceId);

    long count();
}
