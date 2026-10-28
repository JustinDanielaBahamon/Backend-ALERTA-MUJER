package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.AlertMessage;

import java.util.List;

/** Output port: the chat of each alert. */
public interface MessageRepository {

    /** Messages after afterId (0 = all), oldest to newest. */
    List<AlertMessage> posteriores(Long alertId, long afterId);

    long count(Long alertId);

    AlertMessage save(AlertMessage message);
}
