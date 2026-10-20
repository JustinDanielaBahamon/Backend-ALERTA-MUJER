package com.alertamujer.evidence.application.service;

import com.alertamujer.evidence.application.port.in.LimpiarEvidenciasUseCase;
import com.alertamujer.evidence.application.port.out.AlertasPort;
import com.alertamujer.evidence.application.port.out.AlmacenamientoEvidencias;
import com.alertamujer.evidence.application.port.out.EvidenciaRepositorio;
import com.alertamujer.evidence.domain.model.Evidence;
import com.alertamujer.shared.web.AuditSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Borra las evidencias (archivo y registro) a los 60 días, como promete la app, salvo las de alertas
 * que la usuaria marcó para conservar. Así el almacenamiento (B2, 10 GB gratis) no se llena.
 */
@Service
public class LimpiezaEvidenciasService implements LimpiarEvidenciasUseCase {

    private static final Logger log = LoggerFactory.getLogger(LimpiezaEvidenciasService.class);

    private final EvidenciaRepositorio repositorio;
    private final AlmacenamientoEvidencias almacenamiento;
    private final AlertasPort alertas;
    private final int dias;

    public LimpiezaEvidenciasService(EvidenciaRepositorio repositorio, AlmacenamientoEvidencias almacenamiento,
                                     AlertasPort alertas, @Value("${evidence.retention.days:60}") int dias) {
        this.repositorio = repositorio;
        this.almacenamiento = almacenamiento;
        this.alertas = alertas;
        this.dias = dias;
    }

    @Override
    public int limpiar(LocalDateTime ahora) {
        List<Evidence> viejas = repositorio.guardadasAntesDe(ahora.minusDays(dias));
        if (viejas.isEmpty()) return 0;
        Set<Long> conservadas = alertas.conservanEvidencias(
                viejas.stream().map(Evidence::getAlertId).collect(Collectors.toSet()));
        List<Evidence> aBorrar = viejas.stream().filter(e -> !conservadas.contains(e.getAlertId())).toList();
        if (aBorrar.isEmpty()) return 0;
        borrar(aBorrar, "Tarea automática · Limpieza de evidencias");
        log.info("Evidencias de más de {} días eliminadas: {}", dias, aBorrar.size());
        return aBorrar.size();
    }

    @Override
    public int borrarDeUsuaria(Long userProfileId) {
        if (userProfileId == null) return 0;
        List<Evidence> suyas = repositorio.deLasAlertas(alertas.idsDeLaUsuaria(userProfileId));
        if (suyas.isEmpty()) return 0;
        borrar(suyas, "Cuenta eliminada · Borrado de evidencias");
        log.info("Cuenta eliminada: {} evidencias del perfil {} borradas", suyas.size(), userProfileId);
        return suyas.size();
    }

    private void borrar(List<Evidence> evidencias, String origen) {
        AuditSource.con(origen, () -> {
            repositorio.eliminarTodas(evidencias);
            return null;
        });
        // El archivo se borra después del registro: si falla queda un archivo huérfano, nunca un
        // registro que apunte a un archivo que ya no existe
        evidencias.forEach(e -> {
            try {
                almacenamiento.eliminar(e.getStorageKey());
            } catch (RuntimeException ex) {
                log.warn("No se pudo borrar el archivo de la evidencia {}: {}", e.getId(), ex.getMessage());
            }
        });
    }
}
