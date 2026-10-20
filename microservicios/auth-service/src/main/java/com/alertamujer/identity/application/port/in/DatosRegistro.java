package com.alertamujer.identity.application.port.in;

/**
 * Formulario de registro. fechaNacimiento en DD/MM/AAAA, como lo escriben los formularios.
 * aceptaTerminos: true si marcó la casilla; null si la app es anterior y no lo envía.
 */
public record DatosRegistro(String nombre, String email, String password, String telefono,
                            String tipoDocumento, String numeroDocumento, String fechaNacimiento,
                            Boolean aceptaTerminos) {
}
