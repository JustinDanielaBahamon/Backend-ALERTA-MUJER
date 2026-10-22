package com.alertamujer.identity.application.port.out;

/** Puerto de salida: avisos al resto del sistema (auditoría del panel y correo de recuperación). */
public interface IdentityEventsPort {

    /** Inicio, fallo o cierre de sesión, o cambio de contraseña, de una administradora (auditoría). */
    void adminSession(Long accountId, String action, String detail);

    /** El correo con el código lo envía Alert Service (notificaciones). */
    void recoveryCode(String email, String name, String code, int validityMinutes);

    /** Se eliminó una cuenta: los demás servicios borran sus datos personales. */
    void accountDeleted(Long userId, Long userProfileId);
}
