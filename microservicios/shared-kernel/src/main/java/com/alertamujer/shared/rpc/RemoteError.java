package com.alertamujer.shared.rpc;

/** Error que devuelve un microservicio al atender una llamada interna, para volver a lanzarlo del otro lado. */
public record RemoteError(String type, String message) {}
