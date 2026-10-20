package com.alertamujer.evidence.adapter.in.web;

import com.alertamujer.evidence.adapter.in.web.dto.AdminEvidenceView;
import com.alertamujer.evidence.adapter.in.web.dto.EvidenceDto;
import com.alertamujer.evidence.adapter.in.web.dto.EvidenceView;
import com.alertamujer.evidence.application.port.in.AdministrarEvidenciasUseCase;
import com.alertamujer.evidence.application.port.in.ConsultarEvidenciasUseCase;
import com.alertamujer.evidence.application.port.in.ConsultarEvidenciasUseCase.ArchivoEvidencia;
import com.alertamujer.evidence.application.port.in.ModificarEvidenciaUseCase;
import com.alertamujer.evidence.application.port.in.SubirEvidenciaUseCase;
import com.alertamujer.evidence.application.port.out.AlmacenamientoEvidencias.ArchivoEntrante;
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

/** Adaptador de entrada REST: traduce HTTP a casos de uso. No tiene lógica de negocio. */
@RestController
@RequestMapping("/api/evidences")
public class EvidenceController {

    private final SubirEvidenciaUseCase subir;
    private final ConsultarEvidenciasUseCase consultar;
    private final ModificarEvidenciaUseCase modificar;
    private final AdministrarEvidenciasUseCase administrar;

    public EvidenceController(SubirEvidenciaUseCase subir, ConsultarEvidenciasUseCase consultar,
                              ModificarEvidenciaUseCase modificar, AdministrarEvidenciasUseCase administrar) {
        this.subir = subir;
        this.consultar = consultar;
        this.modificar = modificar;
        this.administrar = administrar;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminEvidenceView>> getAllEvidences() {
        return ResponseEntity.ok(administrar.listar().stream().map(AdminEvidenceView::of).toList());
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdministrarEvidenciasUseCase.Estadisticas> getStats() {
        return ResponseEntity.ok(administrar.estadisticas());
    }

    /** Revisión de la administración: pending, verified o rejected. */
    @PatchMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminEvidenceView> review(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(AdminEvidenceView.of(administrar.cambiarRevision(id, body.get("reviewStatus"))));
    }

    /** Evidencias de la usuaria autenticada (más recientes primero). */
    @GetMapping("/me")
    public ResponseEntity<List<EvidenceView>> getMine(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(consultar.deLaUsuaria(userId).stream().map(EvidenceView::of).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvidenceDto> getEvidenceById(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(EvidenceDto.of(consultar.una(id, userId)));
    }

    /** Sube una foto, video, audio o PDF y lo asocia a una alerta de la usuaria. */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EvidenceView> upload(@RequestParam("file") MultipartFile file,
                                               @RequestParam("alertId") Long alertId,
                                               @AuthenticationPrincipal Long userId) {
        ArchivoEntrante archivo = new ArchivoEntrante(file.getOriginalFilename(), file.getContentType(), file.getSize(),
                () -> {
                    try {
                        return file.getInputStream();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
        return ResponseEntity.ok(EvidenceView.of(subir.subir(archivo, alertId, userId)));
    }

    /** Contenido del archivo, solo para la dueña (o la administración). Admite Range para adelantar videos. */
    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getFile(@PathVariable Long id,
                                            @RequestParam(defaultValue = "false") boolean download,
                                            @AuthenticationPrincipal Long userId,
                                            Authentication authentication) {
        ArchivoEvidencia archivo = consultar.archivo(id, userId, esAdmin(authentication));
        Evidence e = archivo.evidencia();
        boolean inline = !download && !"document".equals(e.getMediaType());
        ContentDisposition disposition = (inline ? ContentDisposition.inline() : ContentDisposition.attachment())
                .filename(e.getFileName() == null ? "evidencia-" + id : e.getFileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(e.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(archivo.contenido());
    }

    @PostMapping
    public ResponseEntity<EvidenceDto> createEvidence(@RequestBody EvidenceDto.SinArchivo body,
                                                      @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(EvidenceDto.of(modificar.registrarSinArchivo(body.alertId(), body.mediaType(), body.fileUrl(), userId)));
    }

    /** La dueña le pone un nombre más corto a su evidencia. */
    @PatchMapping("/{id}/name")
    public ResponseEntity<EvidenceView> rename(@PathVariable Long id, @RequestBody Map<String, String> body,
                                               @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(EvidenceView.of(modificar.renombrar(id, body == null ? null : body.get("name"), userId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EvidenceDto> updateEvidence(@PathVariable Long id, @RequestBody EvidenceDto.SinArchivo body,
                                                      @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(EvidenceDto.of(modificar.actualizarSinArchivo(id, body.mediaType(), body.fileUrl(), userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvidence(@PathVariable Long id, @AuthenticationPrincipal Long userId,
                                               Authentication authentication) {
        modificar.eliminar(id, userId, esAdmin(authentication));
        return ResponseEntity.noContent().build();
    }

    private static boolean esAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
