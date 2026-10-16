package com.alertamujer.devices.api;

import java.util.List;

/** Consultas del módulo devices para los demás módulos. */
public interface DevicesApi {

    /** Tokens FCM de los dispositivos de la usuaria dueña del perfil (user_profile.id). */
    List<String> pushTokensOfProfile(Long userProfileId);

    /** Número de dispositivos registrados (estadísticas del panel). */
    long countDevices();
}
