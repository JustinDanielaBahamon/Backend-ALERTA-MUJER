package com.alertamujer.evidence.adapter.in.web;

import com.alertamujer.evidence.adapter.in.web.dto.AdminEvidenceView;
import com.alertamujer.evidence.adapter.in.web.dto.EvidenceDto;
import com.alertamujer.evidence.adapter.in.web.dto.EvidenceView;
import com.alertamujer.evidence.application.port.in.ManageEvidenceUseCase;
import com.alertamujer.evidence.application.port.in.QueryEvidenceUseCase;
import com.alertamujer.evidence.application.port.in.QueryEvidenceUseCase.EvidenceWithContent;
import com.alertamujer.evidence.application.port.in.UpdateEvidenceUseCase;
import com.alertamujer.evidence.application.port.in.UploadEvidenceUseCase;
import com.alertamujer.evidence.application.port.out.EvidenceStorage.IncomingFile;
import com.alertamujer.evidence.domain.model.Evidence;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/** REST input adapter: translates HTTP into use cases. It has no business logic. */
@RestController
@RequestMapping("/api/evidences")
public class EvidenceController {

    private final UploadEvidenceUseCase upload;
    private final QueryEvidenceUseCase query;
    private final UpdateEvidenceUseCase update;
    private final ManageEvidenceUseCase manage;

    public EvidenceController(UploadEvidenceUseCase upload, QueryEvidenceUseCase query,
                              UpdateEvidenceUseCase update, ManageEvidenceUseCase manage) {
        this.upload = upload;
        this.query = query;
        this.update = update;
        this.manage = manage;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminEvidenceView>> getAllEvidences() {
        return ResponseEntity.ok(manage.listAll().stream().map(AdminEvidenceView::of).toList());
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ManageEvidenceUseCase.Stats> getStats() {
        return ResponseEntity.ok(manage.stats());
    }

    /** Review by the administration: pending, verified or rejected. */
    @PatchMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminEvidenceView> review(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(AdminEvidenceView.of(manage.changeReview(id, body.get("reviewStatus"))));
    }

    /** Evidence of the authenticated user (newest first). */
    @GetMapping("/me")
    public ResponseEntity<List<EvidenceView>> getMine(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(query.ofUser(userId).stream().map(EvidenceView::of).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvidenceDto> getEvidenceById(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(EvidenceDto.of(query.one(id, userId)));
    }

    /** Uploads a photo, video, audio or PDF and links it to one of the user's alerts. */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EvidenceView> upload(@RequestParam("file") MultipartFile file,
                                               @RequestParam("alertId") Long alertId,
                                               @AuthenticationPrincipal Long userId) {
        IncomingFile incoming = new IncomingFile(file.getOriginalFilename(), file.getContentType(), file.getSize(),
                () -> {
                    try {
                        return file.getInputStream();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
        return ResponseEntity.ok(EvidenceView.of(upload.upload(incoming, alertId, userId)));
    }

    /** File content, only for the owner (or the administration). Supports Range to seek in videos. */
    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getFile(@PathVariable Long id,
                                            @RequestParam(defaultValue = "false") boolean download,
                                            @AuthenticationPrincipal Long userId,
                                            Authentication authentication) {
        EvidenceWithContent file = query.file(id, userId, esAdmin(authentication));
        Evidence e = file.evidence();
        boolean inline = !download && !"document".equals(e.getMediaType());
        ContentDisposition disposition = (inline ? ContentDisposition.inline() : ContentDisposition.attachment())
                .filename(e.getFileName() == null ? "evidencia-" + id : e.getFileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(e.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(file.content());
    }

    @PostMapping
    public ResponseEntity<EvidenceDto> createEvidence(@RequestBody EvidenceDto.WithoutFile body,
                                                      @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(EvidenceDto.of(update.registerWithoutFile(body.alertId(), body.mediaType(), body.fileUrl(), userId)));
    }

    /** The owner gives her evidence a shorter name. */
    @PatchMapping("/{id}/name")
    public ResponseEntity<EvidenceView> rename(@PathVariable Long id, @RequestBody Map<String, String> body,
                                               @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(EvidenceView.of(update.rename(id, body == null ? null : body.get("name"), userId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EvidenceDto> updateEvidence(@PathVariable Long id, @RequestBody EvidenceDto.WithoutFile body,
                                                      @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(EvidenceDto.of(update.updateWithoutFile(id, body.mediaType(), body.fileUrl(), userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvidence(@PathVariable Long id, @AuthenticationPrincipal Long userId,
                                               Authentication authentication) {
        update.delete(id, userId, esAdmin(authentication));
        return ResponseEntity.noContent().build();
    }

    private static boolean esAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
