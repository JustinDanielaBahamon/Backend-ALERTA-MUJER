package com.alertamujer.devices.api;

import java.util.List;

/** Consultas del módulo devices para los demás módulos. */
public interface DevicesApi {

    /** Tokens FCM de los dispositivos de la usuaria dueña del perfil (user_profile.id). */
    List<String> pushTokensOfProfile(Long userProfileId);

    /** Tokens FCM de los dispositivos de una persona (users.id); vacío si no tiene cuenta o dispositivos. */
    List<String> pushTokensOfUser(Long userId);

    /** Tokens push solo de celulares (sin navegadores web): para pedir la ubicación por GPS. */
    List<String> phonePushTokensOfUser(Long userId);

    /**
     * true si el dispositivo es el celular que la usuaria usó más recientemente (o si no se conoce):
     * con varios celulares, la ubicación del día a día se toma solo de ese.
     */
    boolean isMostRecentPhone(Long userId, Long deviceId);

    /** Número de dispositivos registrados (estadísticas del panel). */
    long countDevices();
}
