package com.alertamujer.shared.rpc;

/** Cabecera con la clave compartida que autoriza las llamadas entre microservicios. */
public final class InternalKey {

    public static final String HEADER = "X-Internal-Key";

    private InternalKey() {}
}
