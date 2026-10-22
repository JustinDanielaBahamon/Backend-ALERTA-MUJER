package com.alertamujer.identity.adapter.in.module;

import com.alertamujer.identity.api.IdentityApi;
import com.alertamujer.identity.api.UserSummary;
import com.alertamujer.identity.application.port.in.ConsultarIdentidadUseCase;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de entrada: el contrato IdentityApi para los otros módulos de Auth Service y para Alert
 * y Evidence (por /internal/rpc). Traduce el dominio al record UserSummary del contrato.
 */
@Component
class IdentityApiAdapter implements IdentityApi {

    private final ConsultarIdentidadUseCase consultas;

    IdentityApiAdapter(ConsultarIdentidadUseCase consultas) {
        this.consultas = consultas;
    }

    @Override
    public Long requireProfileId(Long userId) {
        return findProfileId(userId).orElseThrow(() -> new ResourceNotFoundException("UserProfile for user", userId));
    }

    @Override
    public Optional<Long> findProfileId(Long userId) {
        return consultas.profileOf(userId);
    }

    @Override
    public Long requireAccountId(Long userId) {
        return consultas.cuentaDe(userId).orElseThrow(() -> new ResourceNotFoundException("Account for user", userId));
    }

    @Override
    public Optional<UserSummary> findUser(Long userId) {
        return consultas.usuaria(userId).map(IdentityApiAdapter::resumen);
    }

    @Override
    public Optional<UserSummary> findUserByEmail(String email) {
        return consultas.porCorreo(email).map(IdentityApiAdapter::resumen);
    }

    @Override
    public Optional<UserSummary> findUserByProfileId(Long profileId) {
        return consultas.byProfile(profileId).map(IdentityApiAdapter::resumen);
    }

    @Override
    public Optional<UserSummary> findUserByAccountId(Long accountId) {
        return consultas.porCuenta(accountId).map(IdentityApiAdapter::resumen);
    }

    @Override
    public Map<Long, UserSummary> usersById() {
        return resumenes(consultas.porId());
    }

    @Override
    public Map<Long, UserSummary> usersByProfileId() {
        return resumenes(consultas.porIdDePerfil());
    }

    @Override
    public Map<Long, UserSummary> usersByAccountId() {
        return resumenes(consultas.porIdDeCuenta());
    }

    static UserSummary resumen(User u) {
        return new UserSummary(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getTelephone(),
                u.getRoleId(), u.getCreatedAt());
    }

    private static Map<Long, UserSummary> resumenes(Map<Long, User> porClave) {
        return porClave.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> resumen(e.getValue())));
    }
}
