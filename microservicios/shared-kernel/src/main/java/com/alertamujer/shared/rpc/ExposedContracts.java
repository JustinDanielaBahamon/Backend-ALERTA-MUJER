package com.alertamujer.shared.rpc;

import java.util.List;

/**
 * Contratos (interfaces *Api) que este microservicio atiende para los demás en /internal/rpc.
 * Cada servicio declara el suyo como bean; lo que no esté aquí no se puede invocar desde fuera.
 */
public record ExposedContracts(List<Class<?>> contracts) {}
