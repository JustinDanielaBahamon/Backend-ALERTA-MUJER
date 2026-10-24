package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.Alert;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Output port: where alerts are stored. */
public interface AlertRepository {

    Alert save(Alert alert);

    List<Alert> saveAll(List<Alert> alerts);

    Optional<Alert> find(Long id);

    /** Missing ones are skipped. */
    List<Alert> findMany(Collection<Long> ids);

    List<Alert> all();

    List<Alert> ofUser(Long profile);

    List<Alert> active();

    /** The user's most recent active alert. */
    Optional<Alert> ongoing(Long profile);

    /** The user's alerts marked to keep their evidence. */
    long kept(Long profile);

    void delete(Long id);

    /** A location of the active alert arrived now (its phone is still sending). */
    void markSignal(Long id, LocalDateTime when);
}
