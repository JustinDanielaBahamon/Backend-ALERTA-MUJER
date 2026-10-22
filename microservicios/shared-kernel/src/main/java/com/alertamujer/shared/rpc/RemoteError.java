package com.alertamujer.shared.rpc;

/** Error a microservice returns while serving an internal call, to rethrow it on the other side. */
public record RemoteError(String type, String message) {}
