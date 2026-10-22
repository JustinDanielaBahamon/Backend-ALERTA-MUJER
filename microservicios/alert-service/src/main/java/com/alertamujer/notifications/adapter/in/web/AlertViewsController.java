package com.alertamujer.notifications.adapter.in.web;

import com.alertamujer.notifications.application.port.in.AlertViewsUseCase;
import com.alertamujer.notifications.application.port.out.ViewsRepository.ViewCount;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/** REST input adapter: how many people opened the tracking of an own alert. */
@RestController
public class AlertViewsController {

    private final AlertViewsUseCase views;

    public AlertViewsController(AlertViewsUseCase views) {
        this.views = views;
    }

    @GetMapping("/api/alerts/{id}/views")
    public ResponseEntity<ViewCount> views(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(views.views(id, userId, LocalDateTime.now()));
    }
}
