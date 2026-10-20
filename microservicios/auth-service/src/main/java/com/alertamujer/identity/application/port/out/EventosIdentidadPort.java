package com.alertamujer.identity.application.port.out;

/** Puerto de salida: avisos al resto del sistema (auditoría del panel y correo de recuperación). */
public interface EventosIdentidadPort {

    /** Inicio, fallo o cierre de sesión, o cambio de contraseña, de una administradora (auditoría). */
    void sesionAdmin(Long accountId, String accion, String detalle);

    /** El correo con el código lo envía Alert Service (notificaciones). */
    void codigoRecuperacion(String correo, String nombre, String codigo, int minutosValidez);

    /** Se eliminó una cuenta: los demás servicios borran sus datos personales. */
    void cuentaEliminada(Long userId, Long userProfileId);
}
