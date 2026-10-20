package com.alertamujer.alerts.application.port.out;

import com.alertamujer.alerts.domain.model.AlertContact;

import java.util.List;
import java.util.Optional;

/** Puerto de salida: los envíos de cada alerta (un registro por contacto y canal). */
public interface DestinatarioRepositorio {

    void guardarTodos(List<AlertContact> destinatarios);

    AlertContact guardar(AlertContact destinatario);

    Optional<AlertContact> buscar(Long id);

    List<AlertContact> deLaAlerta(Long alertId);

    /** Alertas en las que se avisó a este correo (el contacto que tiene cuenta en la app). */
    List<Long> alertasAvisadasA(String correo);
}
