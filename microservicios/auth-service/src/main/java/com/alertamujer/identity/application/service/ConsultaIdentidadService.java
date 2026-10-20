package com.alertamujer.identity.application.service;

import com.alertamujer.identity.application.port.in.ConsultarIdentidadUseCase;
import com.alertamujer.identity.application.port.out.CuentaRepositorio;
import com.alertamujer.identity.application.port.out.PerfilRepositorio;
import com.alertamujer.identity.application.port.out.UsuariaRepositorio;
import com.alertamujer.identity.domain.model.Account;
import com.alertamujer.identity.domain.model.User;
import com.alertamujer.identity.domain.model.UserProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Consultas de personas, perfiles y cuentas para los otros módulos y microservicios. */
@Service
@Transactional(readOnly = true)
public class ConsultaIdentidadService implements ConsultarIdentidadUseCase {

    private final UsuariaRepositorio usuarias;
    private final PerfilRepositorio perfiles;
    private final CuentaRepositorio cuentas;

    public ConsultaIdentidadService(UsuariaRepositorio usuarias, PerfilRepositorio perfiles, CuentaRepositorio cuentas) {
        this.usuarias = usuarias;
        this.perfiles = perfiles;
        this.cuentas = cuentas;
    }

    @Override
    public Optional<Long> perfilDe(Long userId) {
        return perfiles.deUsuaria(userId).map(UserProfile::getId);
    }

    @Override
    public Optional<Long> cuentaDe(Long userId) {
        return cuentas.deUsuaria(userId).map(Account::getId);
    }

    @Override
    public Optional<User> usuaria(Long userId) {
        return userId == null ? Optional.empty() : usuarias.buscar(userId);
    }

    @Override
    public Optional<User> porCorreo(String correo) {
        return correo == null ? Optional.empty() : usuarias.porCorreo(correo);
    }

    @Override
    public Optional<User> porPerfil(Long perfilId) {
        return perfilId == null ? Optional.empty() : perfiles.buscar(perfilId).flatMap(p -> usuaria(p.getUserId()));
    }

    @Override
    public Optional<User> porCuenta(Long accountId) {
        return accountId == null ? Optional.empty() : cuentas.buscar(accountId).flatMap(c -> usuaria(c.getUserId()));
    }

    @Override
    public Map<Long, User> porId() {
        return usuarias.todas().stream().collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));
    }

    @Override
    public Map<Long, User> porIdDePerfil() {
        Map<Long, User> porUsuaria = porId();
        return perfiles.todos().stream()
                .filter(p -> porUsuaria.containsKey(p.getUserId()))
                .collect(Collectors.toMap(UserProfile::getId, p -> porUsuaria.get(p.getUserId()), (a, b) -> a));
    }

    @Override
    public Map<Long, User> porIdDeCuenta() {
        Map<Long, User> porUsuaria = porId();
        return cuentas.todas().stream()
                .filter(c -> porUsuaria.containsKey(c.getUserId()))
                .collect(Collectors.toMap(Account::getId, c -> porUsuaria.get(c.getUserId()), (a, b) -> a));
    }
}
