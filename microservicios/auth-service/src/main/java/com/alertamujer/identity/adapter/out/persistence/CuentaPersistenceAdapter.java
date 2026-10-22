package com.alertamujer.identity.adapter.out.persistence;

import com.alertamujer.identity.application.port.out.CuentaRepositorio;
import com.alertamujer.identity.domain.model.Account;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adaptador de salida: las cuentas en PostgreSQL. */
@Component
class CuentaPersistenceAdapter implements CuentaRepositorio {

    private final AccountJpaRepository jpa;

    CuentaPersistenceAdapter(AccountJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Account save(Account cuenta) {
        return aDominio(jpa.save(aFila(cuenta)));
    }

    @Override
    public Optional<Account> find(Long id) {
        return jpa.findById(id).map(CuentaPersistenceAdapter::aDominio);
    }

    @Override
    public Optional<Account> deUsuaria(Long userId) {
        return jpa.findByUserId(userId).map(CuentaPersistenceAdapter::aDominio);
    }

    @Override
    public List<Account> todas() {
        return jpa.findAll().stream().map(CuentaPersistenceAdapter::aDominio).toList();
    }

    static Account aDominio(AccountJpaEntity f) {
        Account d = new Account();
        d.setId(f.getId());
        d.setUserId(f.getUserId());
        d.setPasswordHash(f.getPasswordHash());
        d.setStatus(f.getStatus());
        d.setLastAccess(f.getLastAccess());
        return d;
    }

    static AccountJpaEntity aFila(Account d) {
        AccountJpaEntity f = new AccountJpaEntity();
        f.setId(d.getId());
        f.setUserId(d.getUserId());
        f.setPasswordHash(d.getPasswordHash());
        f.setStatus(d.getStatus());
        f.setLastAccess(d.getLastAccess());
        return f;
    }
}
