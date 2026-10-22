package com.alertamujer.devices.adapter.out.identity;

import com.alertamujer.devices.application.port.out.IdentityPort;
import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/** Adaptador de salida hacia identity (mismo microservicio) por su contrato IdentityApi. */
@Component
class DispositivosIdentidadAdapter implements IdentityPort {

    private final IdentityApi identityApi;

    DispositivosIdentidadAdapter(IdentityApi identityApi) {
        this.identityApi = identityApi;
    }

    @Override
    public Optional<Long> cuentaDe(Long userId) {
        try {
            return Optional.of(identityApi.requireAccountId(userId));
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public Long cuentaRequerida(Long userId) {
        return identityApi.requireAccountId(userId);
    }

    @Override
    public Optional<Long> userOfProfile(Long perfil) {
        return identityApi.findUserByProfileId(perfil).map(UserSummary::id);
    }

    @Override
    public Optional<UserSummary> duenaDeCuenta(Long accountId) {
        return identityApi.findUserByAccountId(accountId);
    }

    @Override
    public Map<Long, UserSummary> duenasPorCuenta() {
        return identityApi.usersByAccountId();
    }
}
