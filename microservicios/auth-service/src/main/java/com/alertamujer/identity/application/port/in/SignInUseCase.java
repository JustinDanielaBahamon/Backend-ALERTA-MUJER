package com.alertamujer.identity.application.port.in;

/** Entrar, renovar y cerrar la sesión. */
public interface SignInUseCase {

    Session withEmail(String email, String password);

    /**
     * Con Google (token de Firebase en la web o de Google en el móvil). Si el correo ya está
     * registrado entra a esa cuenta; si no, crea la usuaria (solo podrá entrar con Google).
     */
    Session conGoogle(String idToken);

    /** Nuevo token de acceso a partir del de renovación. */
    String refresh(String refreshToken);

    /** Cierre voluntario o por inactividad (el JWT no se revoca; queda en la auditoría si es administradora). */
    void close(Long userId, String reason);
}
