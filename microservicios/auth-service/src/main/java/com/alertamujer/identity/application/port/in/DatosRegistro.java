package com.alertamujer.identity.application.port.in;

/** Formulario de registro. fechaNacimiento en DD/MM/AAAA, como lo escriben los formularios. */
public record DatosRegistro(String nombre, String email, String password, String telefono,
                            String tipoDocumento, String numeroDocumento, String fechaNacimiento) {
}
