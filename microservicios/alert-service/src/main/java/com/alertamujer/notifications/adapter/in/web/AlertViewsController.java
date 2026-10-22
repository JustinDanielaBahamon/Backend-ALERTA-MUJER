package com.alertamujer.notifications.adapter.in.web;

import com.alertamujer.notifications.application.port.in.VistasAlertaUseCase;
import com.alertamujer.notifications.application.port.out.VistasRepositorio.Conteo;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/** Adaptador de entrada REST: cuántas personas abrieron el seguimiento de una alerta propia. */
@RestController
public class AlertViewsController {

    private final VistasAlertaUseCase views;

    public AlertViewsController(VistasAlertaUseCase views) {
        this.views = views;
    }

    @GetMapping("/api/alerts/{id}/views")
    public ResponseEntity<Conteo> views(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(views.views(id, userId, LocalDateTime.now()));
    }
}
