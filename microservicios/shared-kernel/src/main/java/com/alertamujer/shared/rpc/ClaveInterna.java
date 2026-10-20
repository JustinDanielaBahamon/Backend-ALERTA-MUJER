package com.alertamujer.shared.rpc;

/** Cabecera con la clave compartida que autoriza las llamadas entre microservicios. */
public final class ClaveInterna {

    public static final String CABECERA = "X-Internal-Key";

    private ClaveInterna() {}
}
