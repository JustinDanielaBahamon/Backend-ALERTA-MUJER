package com.alertamujer.identity.application.port.out;

import com.alertamujer.identity.domain.model.DeviceSession;

import java.util.List;

/** Output port: device sessions. */
public interface SessionRepository {

    List<DeviceSession> activeForAccount(Long accountId);

    DeviceSession save(DeviceSession session);
}
