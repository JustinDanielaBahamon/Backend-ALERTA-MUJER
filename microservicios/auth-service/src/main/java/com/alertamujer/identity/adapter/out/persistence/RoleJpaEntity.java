package com.alertamujer.identity.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Fila de un rol (1 = user, 2 = admin). En el dominio el rol es un dato de User. */
@Entity
@Table(name = "role", schema = "identity")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class RoleJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;
}
