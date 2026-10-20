package com.alertamujer.admin.adapter.in.web.dto;

import com.alertamujer.admin.domain.model.CambiosInforme;

import java.time.LocalDateTime;
import java.util.List;

/** Body para crear o editar un informe de alertas. */
public record AlertReportRequest(String name, String city, String status, String userLabel, String event,
                                 String description, String format, List<Long> alertIds,
                                 LocalDateTime fromDate, LocalDateTime toDate, String alertStatus) {

    public CambiosInforme aCambios() {
        return new CambiosInforme(name, city, status, userLabel, event, description, format, alertIds, fromDate, toDate, alertStatus);
    }
}
