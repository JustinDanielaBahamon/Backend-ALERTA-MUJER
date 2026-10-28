package com.alertamujer.evidence.application.port.in;

import com.alertamujer.evidence.application.port.out.EvidenceStorage.IncomingFile;

/** Use case: the user uploads a photo, video, audio or PDF to one of her alerts. */
public interface UploadEvidenceUseCase {

    EvidenceWithAlert upload(IncomingFile file, Long alertId, Long userId);
}
