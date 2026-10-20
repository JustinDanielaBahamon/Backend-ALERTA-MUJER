package com.alertamujer.evidence.application.port.in;

import com.alertamujer.evidence.application.port.out.AlmacenamientoEvidencias.ArchivoEntrante;

/** Caso de uso: la usuaria sube una foto, video, audio o PDF a una de sus alertas. */
public interface SubirEvidenciaUseCase {

    EvidenciaConAlerta subir(ArchivoEntrante archivo, Long alertId, Long userId);
}
