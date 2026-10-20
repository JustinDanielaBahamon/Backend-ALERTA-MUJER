package com.alertamujer.admin.application.port.in;

/**
 * Eventos de seguridad de las cuentas de administración (inicios y cierres de sesión, intentos
 * fallidos, cambio de contraseña). Solo cuentas admin: las usuarias de la app no llenan la auditoría.
 */
public interface RegistrarEventoSeguridadUseCase {

    /** Nunca falla: un error al auditar no debe impedir iniciar sesión. */
    void registrar(Long accountId, String accion, String detalle);
}
