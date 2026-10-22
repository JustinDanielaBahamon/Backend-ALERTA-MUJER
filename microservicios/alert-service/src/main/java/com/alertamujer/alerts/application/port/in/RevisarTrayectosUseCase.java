package com.alertamujer.alerts.application.port.in;

import java.time.LocalDateTime;

/** Revisión periódica: pregunta "¿ya llegaste?" y alerta por quien no confirmó a tiempo. */
public interface RevisarTrayectosUseCase {

    /** Devuelve cuántas alertas activó. */
    int revisar(LocalDateTime ahora);
}
