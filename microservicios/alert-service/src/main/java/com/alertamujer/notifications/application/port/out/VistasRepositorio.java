package com.alertamujer.notifications.application.port.out;

import java.time.LocalDateTime;

/** Puerto de salida: quién abrió el seguimiento de cada alerta. */
public interface VistasRepositorio {

    /** Registra (o renueva) la visita de ese visitante a la alerta. */
    void registrar(Long alertId, String visitante, LocalDateTime ahora);

    /** total: visitantes distintos; viendoAhora: los que la abrieron desde "recientes". */
    Conteo contar(Long alertId, LocalDateTime recientes);

    record Conteo(int total, int viendoAhora) {}
}
