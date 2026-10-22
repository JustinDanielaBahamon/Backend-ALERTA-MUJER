package com.alertamujer.identity.application.port.out;

import com.alertamujer.identity.domain.model.RecoveryRequest;

import java.util.List;

/** Output port: password recovery requests. */
public interface RecoveryRepository {

    RecoveryRequest save(RecoveryRequest deletionRequest);

    void saveAll(List<RecoveryRequest> deletionRequests);

    List<RecoveryRequest> pendingForAccount(Long accountId);

    boolean hashExists(String hash);
}
