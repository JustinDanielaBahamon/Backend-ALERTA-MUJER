package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.AlertContact;

import java.util.List;
import java.util.Optional;

/** Output port: the deliveries of each alert (one record per contact and channel). */
public interface RecipientRepository {

    void saveAll(List<AlertContact> recipients);

    AlertContact save(AlertContact recipient);

    Optional<AlertContact> find(Long id);

    List<AlertContact> ofAlert(Long alertId);

    /** Alerts in which this email was notified (the contact who has an app account). */
    List<Long> alertsNotifiedTo(String email);
}
