package com.alertamujer.shared.rpc;

/** Header with the shared key that authorizes calls between microservices. */
public final class InternalKey {

    public static final String HEADER = "X-Internal-Key";

    private InternalKey() {}
}
