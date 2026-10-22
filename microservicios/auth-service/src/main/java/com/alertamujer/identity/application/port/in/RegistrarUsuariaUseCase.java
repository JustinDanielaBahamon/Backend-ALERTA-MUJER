package com.alertamujer.identity.application.port.in;

/** Registro con correo y contraseña. */
public interface RegistrarUsuariaUseCase {

    /**
     * Crea la persona, su cuenta y su perfil y abre la sesión. Si el correo, el teléfono o el
     * documento ya los usa otra cuenta, responde con todos los campos repetidos a la vez.
     */
    Sesion registrar(DatosRegistro data);
}
