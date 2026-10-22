package com.alertamujer.admin.application.port.out;

import com.alertamujer.admin.domain.model.UserReport;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: los casos de moderación. */
public interface UserReportRepository {

    List<UserReport> all();

    List<UserReport> withStatus(String status);

    Optional<UserReport> find(Long id);

    /** Casos sobre una usuaria (users.id). */
    List<UserReport> ofUser(Long userId);

    UserReport save(UserReport userReport);

    void delete(Long id);
}
