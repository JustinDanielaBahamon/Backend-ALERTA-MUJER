package com.alertamujer.notifications.application.port.out;

import java.util.List;
import java.util.Map;

/** Output port: high-priority push with Firebase. */
public interface PushPort {

    /** Returns the tokens that could not be reached. */
    List<String> send(List<String> tokens, Map<String, String> data);
}
