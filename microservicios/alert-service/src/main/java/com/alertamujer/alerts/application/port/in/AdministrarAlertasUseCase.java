package com.alertamujer.alerts.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.alertamujer.alerts.api.AdminAlertView;
import com.alertamujer.alerts.domain.model.Alert;

import java.util.List;

/** Casos de uso del panel de administración. */
public interface AdministrarAlertasUseCase {

    /** Todas las alertas con la usuaria que la activó, de la más reciente a la más antigua. */
    List<AdminAlertView> listAll();

    /** Cambia el estado de cualquier alerta, por ejemplo a "resolved" (atendida). */
    Alert cambiarEstado(Long id, String status);

    /**
     * Detalle para el panel: última ubicación (solo de alertas activas o atendidas, auditoría S2) y
     * estado de envío a cada contacto, sin su nombre ni su correo.
     */
    DetalleAdmin detalle(Long id);

    record UltimaUbicacion(BigDecimal latitude, BigDecimal longitude, BigDecimal accuracy, LocalDateTime recordedAt) {}

    record CanalAdmin(String channel, String status, LocalDateTime sentAt, LocalDateTime deliveredAt) {}

    /** contacto: número del contacto en la alerta (1, 2...). relationship: parentesco, si lo tiene. */
    record EnvioAdmin(int contacto, String relationship, List<CanalAdmin> channels) {}

    /** ubicacionVisible: false si la alerta se canceló o venció (la administración no ve su recorrido). */
    record DetalleAdmin(boolean ubicacionVisible, int puntos, UltimaUbicacion ultimaUbicacion, List<EnvioAdmin> envios) {}
}
