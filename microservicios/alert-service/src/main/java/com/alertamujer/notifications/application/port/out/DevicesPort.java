package com.alertamujer.notifications.application.port.out;

import java.util.List;

/** Output port to devices (Auth Service over HTTP). */
public interface DevicesPort {

    /** Push tokens of all the person's devices (users.id). */
    List<String> tokensOf(Long userId);
}
