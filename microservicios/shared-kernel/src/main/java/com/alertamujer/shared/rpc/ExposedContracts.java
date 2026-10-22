package com.alertamujer.shared.rpc;

import java.util.List;

/**
 * Contracts (*Api interfaces) this microservice serves to the others at /internal/rpc.
 * Each service declares its own as a bean; anything not listed here cannot be invoked from outside.
 */
public record ExposedContracts(List<Class<?>> contracts) {}
