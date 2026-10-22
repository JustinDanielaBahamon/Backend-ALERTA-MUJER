package com.alertamujer.admin.application.port.out;

import com.alertamujer.admin.domain.model.ModerationAction;

import java.util.Collection;
import java.util.List;

/** Puerto de salida: las acciones de moderación de cada caso. */
public interface ModerationActionRepository {

    List<ModerationAction> fromCases(Collection<Long> caseIds);

    ModerationAction save(ModerationAction action);
}
