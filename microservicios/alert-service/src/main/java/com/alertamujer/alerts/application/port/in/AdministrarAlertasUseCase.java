package com.alertamujer.alerts.application.port.in;

import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.alerts.domain.model.Alert;

import java.util.List;

/** Casos de uso del panel de administración. */
public interface AdministrarAlertasUseCase {

    /** Todas las alertas con la usuaria que la activó, de la más reciente a la más antigua. */
    List<AdminAlertView> listar();

    /** Cambia el estado de cualquier alerta, por ejemplo a "resolved" (atendida). */
    Alert cambiarEstado(Long id, String estado);
}
