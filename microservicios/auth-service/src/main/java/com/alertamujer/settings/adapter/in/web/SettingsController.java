package com.alertamujer.settings.adapter.in.web;

import com.alertamujer.settings.adapter.in.web.dto.PublicSettings;
import com.alertamujer.settings.adapter.in.web.dto.SettingsRequest;
import com.alertamujer.settings.adapter.in.web.dto.SettingsView;
import com.alertamujer.settings.application.port.in.ManageSettingsUseCase;
import com.alertamujer.settings.application.port.in.QuerySettingsUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** REST input adapter: the administration edits the parameters and signed-in apps read them. */
@RestController
public class SettingsController {

    private final ManageSettingsUseCase administration;
    private final QuerySettingsUseCase queries;

    public SettingsController(ManageSettingsUseCase administration, QuerySettingsUseCase queries) {
        this.administration = administration;
        this.queries = queries;
    }

    @GetMapping("/api/admin/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SettingsView> get() {
        return ResponseEntity.ok(SettingsView.of(administration.get()));
    }

    @PutMapping("/api/admin/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SettingsView> update(@RequestBody SettingsRequest request, @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(SettingsView.of(administration.update(request.toChanges(), currentUserId)));
    }

    @GetMapping("/api/settings")
    public ResponseEntity<PublicSettings> publicSettings() {
        return ResponseEntity.ok(PublicSettings.of(queries.publicPaths()));
    }
}
