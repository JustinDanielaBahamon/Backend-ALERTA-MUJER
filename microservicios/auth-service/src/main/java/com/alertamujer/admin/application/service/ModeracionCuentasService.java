package com.alertamujer.admin.application.service;

import com.alertamujer.admin.application.port.in.ModeracionCuentasUseCase;
import com.alertamujer.admin.application.port.out.AccionModeracionRepositorio;
import com.alertamujer.admin.application.port.out.CuentasPort;
import com.alertamujer.admin.application.port.out.ReporteUsuariaRepositorio;
import com.alertamujer.admin.application.port.out.SolicitudEliminacionRepositorio;
import com.alertamujer.admin.application.port.out.UsuariasPort;
import com.alertamujer.admin.domain.model.AccountDeletionRequest;
import com.alertamujer.admin.domain.model.ModerationAction;
import com.alertamujer.admin.domain.model.TextoModeracion;
import com.alertamujer.admin.domain.model.UserReport;
import com.alertamujer.identity.api.UserAccountChanges;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import com.alertamujer.shared.web.FotoDataUri;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Bloqueos con motivo y eliminación de cuentas con aprobación de una segunda administradora. */
@Service
public class ModeracionCuentasService implements ModeracionCuentasUseCase {

    private static final String MINIMO = " (mínimo " + TextoModeracion.MINIMO_EXPLICACION + " caracteres)";

    private final ReporteUsuariaRepositorio casos;
    private final AccionModeracionRepositorio acciones;
    private final SolicitudEliminacionRepositorio solicitudes;
    private final CuentasPort cuentas;
    private final UsuariasPort usuarias;

    public ModeracionCuentasService(ReporteUsuariaRepositorio casos, AccionModeracionRepositorio acciones,
                                    SolicitudEliminacionRepositorio solicitudes, CuentasPort cuentas, UsuariasPort usuarias) {
        this.casos = casos;
        this.acciones = acciones;
        this.solicitudes = solicitudes;
        this.cuentas = cuentas;
        this.usuarias = usuarias;
    }

    // ── Bloqueo ────────────────────────────────────────────────────

    @Override
    @Transactional
    public void bloquear(Long userId, Bloqueo bloqueo, Long adminId) {
        String motivo = TextoModeracion.requerido(bloqueo == null ? null : bloqueo.reason(), "Elige el motivo del bloqueo", 100);
        String detalle = TextoModeracion.explicacion(bloqueo.description(), "Explica por qué se bloquea la cuenta" + MINIMO);
        String evidencia = FotoDataUri.validar(bloqueo.evidence());
        existente(userId);
        // identity impide que una administradora se bloquee a sí misma
        cuentas.actualizar(userId, new UserAccountChanges("blocked", null, null, null, null), adminId);
        UserReport caso = casos.guardar(UserReport.bloqueo(userId, adminId, motivo, detalle, evidencia, LocalDateTime.now()));
        acciones.guardar(ModerationAction.de(caso.getId(), adminId, ModerationAction.BLOQUEO, detalle));
    }

    @Override
    @Transactional
    public void desbloquear(Long userId, String nota, Long adminId) {
        String detalle = TextoModeracion.explicacion(nota, "Explica por qué se desbloquea la cuenta" + MINIMO);
        existente(userId);
        cuentas.actualizar(userId, new UserAccountChanges("active", null, null, null, null), adminId);
        // Se anota en el último caso de la usuaria; si no tiene (bloqueo anterior a este registro), se crea uno
        UserReport caso = casos.deUsuaria(userId).stream()
                .max(Comparator.comparing(UserReport::getCreatedAt))
                .orElseGet(() -> casos.guardar(UserReport.desbloqueoSinCaso(userId, adminId, detalle, LocalDateTime.now())));
        acciones.guardar(ModerationAction.de(caso.getId(), adminId, ModerationAction.DESBLOQUEO, detalle));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialItem> historial(Long userId) {
        Map<Long, UserReport> porId = casos.deUsuaria(userId).stream().collect(Collectors.toMap(UserReport::getId, r -> r));
        Map<Long, UserSummary> nombres = usuarias.porId();
        Stream<HistorialItem> deAcciones = porId.isEmpty() ? Stream.empty()
                : acciones.deCasos(porId.keySet()).stream().map(a -> {
                    UserReport caso = porId.get(a.getUserReportId());
                    return new HistorialItem(a.getCreatedAt(), a.getActionType(), nombre(nombres.get(a.getAdminPerfilId().longValue())),
                            a.esBloqueo() ? caso.getReason() : null, a.getNotes(), caso.getId(), caso.tieneEvidencia());
                });
        Stream<HistorialItem> deSolicitudes = solicitudes.todas().stream()
                .filter(s -> s.getUserId().equals(userId))
                .map(s -> new HistorialItem(s.getCreatedAt(), "deletion_" + s.getStatus(), nombre(nombres.get(s.getRequestedBy())),
                        null, s.getReason(), null, false));
        return Stream.concat(deAcciones, deSolicitudes).sorted(Comparator.comparing(HistorialItem::fecha).reversed()).toList();
    }

    // ── Eliminación con dos administradoras ────────────────────────

    @Override
    @Transactional
    public SolicitudView solicitarEliminacion(Long userId, String motivo, Long adminId) {
        String detalle = TextoModeracion.explicacion(motivo, "Explica por qué se elimina la cuenta" + MINIMO);
        existente(userId);
        AccountDeletionRequest nueva = AccountDeletionRequest.nueva(userId, adminId, detalle);
        if (solicitudes.hayPendiente(userId)) throw new IllegalArgumentException("Ya hay una solicitud pendiente para esta cuenta");
        return vista(solicitudes.guardar(nueva), usuarias.porId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudView> solicitudes() {
        Map<Long, UserSummary> nombres = usuarias.porId();
        return solicitudes.todas().stream().map(s -> vista(s, nombres)).toList();
    }

    @Override
    @Transactional
    public SolicitudView aprobar(Long solicitudId, String nota, Long adminId) {
        AccountDeletionRequest s = existenteSolicitud(solicitudId);
        s.aprobar(adminId, nota, LocalDateTime.now());
        cuentas.anonimizar(s.getUserId());
        return vista(solicitudes.guardar(s), usuarias.porId());
    }

    @Override
    @Transactional
    public SolicitudView rechazar(Long solicitudId, String nota, Long adminId) {
        AccountDeletionRequest s = existenteSolicitud(solicitudId);
        s.rechazar(adminId, nota, LocalDateTime.now());
        return vista(solicitudes.guardar(s), usuarias.porId());
    }

    // ── Apoyo ──────────────────────────────────────────────────────

    private void existente(Long userId) {
        usuarias.buscar(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private AccountDeletionRequest existenteSolicitud(Long id) {
        return solicitudes.buscar(id).orElseThrow(() -> new ResourceNotFoundException("AccountDeletionRequest", id));
    }

    private static String nombre(UserSummary u) {
        return u == null ? null : u.fullName();
    }

    private static SolicitudView vista(AccountDeletionRequest s, Map<Long, UserSummary> nombres) {
        UserSummary cuenta = nombres.get(s.getUserId());
        return new SolicitudView(s.getId(), s.getUserId(), nombre(cuenta), cuenta != null ? cuenta.email() : null,
                s.getRequestedBy(), nombre(nombres.get(s.getRequestedBy())), s.getReason(), s.getStatus(),
                s.getDecidedBy(), s.getDecidedBy() == null ? null : nombre(nombres.get(s.getDecidedBy())),
                s.getDecisionNote(), s.getCreatedAt(), s.getDecidedAt());
    }
}
