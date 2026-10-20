package com.alertamujer.evidence.application.service;

import com.alertamujer.alerts.api.AlertSummary;
import com.alertamujer.evidence.application.port.in.ConsultarEvidenciasUseCase;
import com.alertamujer.evidence.application.port.in.EvidenciaConAlerta;
import com.alertamujer.evidence.application.port.in.ModificarEvidenciaUseCase;
import com.alertamujer.evidence.application.port.in.SubirEvidenciaUseCase;
import com.alertamujer.evidence.application.port.out.AlertasPort;
import com.alertamujer.evidence.application.port.out.AlmacenamientoEvidencias;
import com.alertamujer.evidence.application.port.out.AlmacenamientoEvidencias.ArchivoEntrante;
import com.alertamujer.evidence.application.port.out.AlmacenamientoEvidencias.ArchivoGuardado;
import com.alertamujer.evidence.application.port.out.EvidenciaRepositorio;
import com.alertamujer.evidence.application.port.out.IdentidadPort;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.shared.exception.AccessDeniedException;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Casos de uso de la usuaria con sus evidencias. Solo conoce puertos, no la base ni el almacenamiento. */
@Service
public class EvidenciasService implements SubirEvidenciaUseCase, ConsultarEvidenciasUseCase, ModificarEvidenciaUseCase {

    private final EvidenciaRepositorio repositorio;
    private final AlmacenamientoEvidencias almacenamiento;
    private final AlertasPort alertas;
    private final IdentidadPort identidad;
    private final long maxSizeBytes;

    public EvidenciasService(EvidenciaRepositorio repositorio, AlmacenamientoEvidencias almacenamiento,
                             AlertasPort alertas, IdentidadPort identidad,
                             @Value("${evidence.max-size-bytes}") long maxSizeBytes) {
        this.repositorio = repositorio;
        this.almacenamiento = almacenamiento;
        this.alertas = alertas;
        this.identidad = identidad;
        this.maxSizeBytes = maxSizeBytes;
    }

    // ── Subir ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public EvidenciaConAlerta subir(ArchivoEntrante archivo, Long alertId, Long userId) {
        if (alertId == null) throw new IllegalArgumentException("Indica a qué alerta pertenece la evidencia");
        if (archivo == null || archivo.tamano() <= 0) throw new IllegalArgumentException("El archivo está vacío");
        if (archivo.tamano() > maxSizeBytes) {
            throw new IllegalArgumentException("El archivo supera el máximo de " + (maxSizeBytes / (1024 * 1024)) + " MB");
        }
        String contentType = archivo.contentType() == null ? "" : archivo.contentType().toLowerCase();
        String tipo = Evidence.tipoDe(contentType);
        AlertSummary alerta = alertaPropia(alertId, userId);

        ArchivoGuardado guardado = almacenamiento.guardar(archivo);
        try {
            Evidence e = new Evidence();
            e.setAlertId(alerta.id());
            e.setMediaType(tipo);
            e.setContentType(contentType);
            e.setFileName(Evidence.nombreSeguro(archivo.nombreOriginal(), tipo));
            e.setSizeBytes(guardado.size());
            e.setSha256(guardado.sha256());
            e.setStorageKey(guardado.key());
            e.setFileUrl("pending");
            e = repositorio.guardar(e);
            e.setFileUrl("/api/evidences/" + e.getId() + "/file");
            return new EvidenciaConAlerta(repositorio.guardar(e), alerta);
        } catch (RuntimeException ex) {
            almacenamiento.eliminar(guardado.key()); // sin registro no debe quedar un archivo huérfano
            throw ex;
        }
    }

    // ── Consultar ───────────────────────────────────────────────────

    @Override
    public List<EvidenciaConAlerta> deLaUsuaria(Long userId) {
        List<Long> ids = alertas.idsDeLaUsuaria(identidad.perfilDe(userId));
        List<Evidence> evidencias = repositorio.deLasAlertas(ids);
        Map<Long, AlertSummary> porId = alertas.buscarVarias(
                evidencias.stream().map(Evidence::getAlertId).collect(Collectors.toSet()));
        return evidencias.stream().map(e -> new EvidenciaConAlerta(e, porId.get(e.getAlertId()))).toList();
    }

    @Override
    public Evidence una(Long id, Long userId) {
        Evidence e = existente(id);
        alertaPropia(e.getAlertId(), userId);
        return e;
    }

    @Override
    public ArchivoEvidencia archivo(Long id, Long userId, boolean admin) {
        Evidence e = admin ? existente(id) : una(id, userId);
        if (!e.tieneArchivo()) throw new ResourceNotFoundException("Archivo de la evidencia " + id);
        Resource contenido = almacenamiento.cargar(e.getStorageKey());
        if (!contenido.exists()) throw new ResourceNotFoundException("Archivo de la evidencia " + id);
        return new ArchivoEvidencia(e, contenido);
    }

    // ── Modificar ───────────────────────────────────────────────────

    @Override
    @Transactional
    public EvidenciaConAlerta renombrar(Long id, String nombre, Long userId) {
        Evidence e = una(id, userId);
        e.renombrar(nombre);
        return new EvidenciaConAlerta(repositorio.guardar(e), alertaPropia(e.getAlertId(), userId));
    }

    @Override
    @Transactional
    public void eliminar(Long id, Long userId, boolean admin) {
        Evidence e = admin ? existente(id) : una(id, userId);
        repositorio.eliminar(e);
        almacenamiento.eliminar(e.getStorageKey());
    }

    @Override
    public Evidence registrarSinArchivo(Long alertId, String mediaType, String fileUrl, Long userId) {
        alertaPropia(alertId, userId);
        Evidence e = new Evidence();
        e.setAlertId(alertId);
        e.setMediaType(mediaType);
        e.setFileUrl(fileUrl);
        return repositorio.guardar(e);
    }

    @Override
    public Evidence actualizarSinArchivo(Long id, String mediaType, String fileUrl, Long userId) {
        Evidence e = una(id, userId);
        if (e.tieneArchivo()) throw new IllegalArgumentException("Una evidencia con archivo no se puede modificar");
        e.setMediaType(mediaType);
        e.setFileUrl(fileUrl);
        return repositorio.guardar(e);
    }

    // ── Apoyo ───────────────────────────────────────────────────────

    private Evidence existente(Long id) {
        return repositorio.buscar(id).orElseThrow(() -> new ResourceNotFoundException("Evidence", id));
    }

    private AlertSummary alertaPropia(Long alertId, Long userId) {
        AlertSummary alerta = alertas.buscar(alertId).orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        if (!alerta.userProfileId().equals(identidad.perfilDe(userId))) {
            throw new AccessDeniedException("No puedes acceder a evidencias de alertas de otro usuario");
        }
        return alerta;
    }
}
