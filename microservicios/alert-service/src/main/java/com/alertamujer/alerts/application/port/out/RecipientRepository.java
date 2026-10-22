package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.AlertContact;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: los envíos de cada alerta (un registro por contacto y canal). */
public interface RecipientRepository {

    void saveAll(List<AlertContact> recipients);

    AlertContact save(AlertContact recipient);

    Optional<AlertContact> find(Long id);

    List<AlertContact> ofAlert(Long alertId);

    /** Alertas en las que se avisó a este correo (el contacto que tiene cuenta en la app). */
    List<Long> alertsNotifiedTo(String email);
}
