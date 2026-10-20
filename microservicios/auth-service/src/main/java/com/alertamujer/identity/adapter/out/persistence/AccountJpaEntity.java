package com.alertamujer.identity.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** Fila de una cuenta (credenciales). El dominio es Account. */
@Entity
@Table(name = "account", schema = "identity")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class AccountJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    private String status;

    @Column(name = "last_access")
    private LocalDateTime lastAccess;
}
