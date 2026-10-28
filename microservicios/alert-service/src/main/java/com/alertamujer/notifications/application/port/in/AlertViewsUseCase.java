package com.alertamujer.notifications.application.port.in;

import com.alertamujer.notifications.application.port.out.ViewsRepository.ViewCount;

import java.time.LocalDateTime;

/** How many people opened the tracking of an own alert (and how many are watching it). */
public interface AlertViewsUseCase {

    ViewCount views(Long alertId, Long userId, LocalDateTime now);
}
