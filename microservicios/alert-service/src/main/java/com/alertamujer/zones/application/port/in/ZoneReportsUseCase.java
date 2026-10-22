package com.alertamujer.zones.application.port.in;

import com.alertamujer.zones.domain.model.ZoneReport;

import java.util.List;
import java.util.Optional;

/** Reportes de las usuarias sobre las zonas y su revisión por la administración. */
public interface ZoneReportsUseCase {

    List<ZoneReport> all();

    /** Vacío si no existe o es de otra usuaria. */
    Optional<ZoneReport> one(Long id, Long userId);

    List<ZoneReport> ofZone(Long zoneId);

    /**
     * Reportes de una usuaria: se acepta su id de usuaria o de perfil; los de otra persona solo los ve
     * la administración.
     */
    List<ZoneReport> ofUser(Long requestedId, Long userId, boolean admin);

    List<ZoneReport> withStatus(String status);

    ZoneReport create(Long zoneId, String classification, String comment, Long userId);

    ZoneReport approve(Long id, Integer adminId);

    ZoneReport reject(Long id, Integer adminId);

    void delete(Long id, Long userId);
}
