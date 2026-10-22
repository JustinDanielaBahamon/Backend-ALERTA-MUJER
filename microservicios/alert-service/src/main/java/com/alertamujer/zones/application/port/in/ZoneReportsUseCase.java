package com.alertamujer.zones.application.port.in;

import com.alertamujer.zones.domain.model.ZoneReport;

import java.util.List;
import java.util.Optional;

/** Users' reports about zones and their review by the administration. */
public interface ZoneReportsUseCase {

    List<ZoneReport> all();

    /** Empty if it does not exist or belongs to another user. */
    Optional<ZoneReport> one(Long id, Long userId);

    List<ZoneReport> ofZone(Long zoneId);

    /**
     * Reports of a user: her user id or profile id is accepted; another person's are only seen
     * by the administration.
     */
    List<ZoneReport> ofUser(Long requestedId, Long userId, boolean admin);

    List<ZoneReport> withStatus(String status);

    ZoneReport create(Long zoneId, String classification, String comment, Long userId);

    ZoneReport approve(Long id, Integer adminId);

    ZoneReport reject(Long id, Integer adminId);

    void delete(Long id, Long userId);
}
