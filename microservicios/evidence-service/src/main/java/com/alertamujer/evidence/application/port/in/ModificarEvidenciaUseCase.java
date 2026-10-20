package com.alertamujer.evidence.application.port.in;

import com.alertamujer.evidence.domain.model.Evidence;

/** Casos de uso de cambios de la usuaria (y borrado por la administración). */
public interface ModificarEvidenciaUseCase {

    EvidenciaConAlerta renombrar(Long id, String nombre, Long userId);

    /** Si admin es true se borra cualquier evidencia; si no, solo una de la usuaria. */
    void eliminar(Long id, Long userId, boolean admin);

    /** Registro sin archivo (clientes antiguos que solo envían una URL). */
    Evidence registrarSinArchivo(Long alertId, String mediaType, String fileUrl, Long userId);

    Evidence actualizarSinArchivo(Long id, String mediaType, String fileUrl, Long userId);
}
