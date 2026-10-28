package com.alertamujer.zones.application.port.out;

import com.alertamujer.zones.domain.model.ZoneReport;

import java.util.List;
import java.util.Optional;

/** Output port: the users' reports about the zones. */
public interface ZoneReportRepository {

    List<ZoneReport> all();

    Optional<ZoneReport> find(Long id);

    List<ZoneReport> ofZone(Long zoneId);

    List<ZoneReport> ofProfile(Long profile);

    List<ZoneReport> withStatus(String status);

    ZoneReport save(ZoneReport userReport);

    void delete(Long id);
}
