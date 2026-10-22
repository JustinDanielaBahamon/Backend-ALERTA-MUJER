package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.EmergencyContact;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Output port: emergency contacts. */
public interface ContactRepository {

    List<EmergencyContact> all();

    Optional<EmergencyContact> find(Long id);

    List<EmergencyContact> findSeveral(Collection<Long> ids);

    List<EmergencyContact> ofUser(Long profile);

    EmergencyContact save(EmergencyContact contact);

    void delete(Long id);
}
