package com.alertamujer.notifications.application.port.in;

import com.alertamujer.notifications.application.port.out.ViewsRepository.ViewCount;

import java.time.LocalDateTime;

/** Cuántas personas abrieron el seguimiento de una alerta propia (y cuántas lo están viendo). */
public interface AlertViewsUseCase {

    ViewCount views(Long alertId, Long userId, LocalDateTime now);
}
