package com.alertamujer.admin.application.port.out;

import java.time.LocalDateTime;

/** Output port: status of the server's technical pieces. */
public interface InfrastructurePort {

    boolean databaseResponds();

    /** Firebase configured: without it no push notifications go out. */
    boolean pushConfigured();

    LocalDateTime startup();

    int versionJava();
}
