package com.alertamujer.identity.adapter.out.persistence;

import com.alertamujer.identity.application.port.out.AccountRepository;
import com.alertamujer.identity.domain.model.Account;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Adaptador de salida: las cuentas en PostgreSQL. */
@Component
class AccountPersistenceAdapter implements AccountRepository {

    private final AccountJpaRepository jpa;

    AccountPersistenceAdapter(AccountJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Account save(Account account) {
        return toDomain(jpa.save(toRow(account)));
    }

    @Override
    public Optional<Account> find(Long id) {
        return jpa.findById(id).map(AccountPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Account> ofUser(Long userId) {
        return jpa.findByUserId(userId).map(AccountPersistenceAdapter::toDomain);
    }

    @Override
    public List<Account> all() {
        return jpa.findAll().stream().map(AccountPersistenceAdapter::toDomain).toList();
    }

    static Account toDomain(AccountJpaEntity f) {
        Account d = new Account();
        d.setId(f.getId());
        d.setUserId(f.getUserId());
        d.setPasswordHash(f.getPasswordHash());
        d.setStatus(f.getStatus());
        d.setLastAccess(f.getLastAccess());
        return d;
    }

    static AccountJpaEntity toRow(Account d) {
        AccountJpaEntity f = new AccountJpaEntity();
        f.setId(d.getId());
        f.setUserId(d.getUserId());
        f.setPasswordHash(d.getPasswordHash());
        f.setStatus(d.getStatus());
        f.setLastAccess(d.getLastAccess());
        return f;
    }
}
