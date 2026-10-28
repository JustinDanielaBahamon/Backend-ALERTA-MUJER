package com.alertamujer.identity.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Row of a role (1 = user, 2 = admin). In the domain the role is a field of User. */
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
