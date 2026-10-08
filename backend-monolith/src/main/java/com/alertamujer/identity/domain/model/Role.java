package com.alertamujer.identity.domain.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Role entity following 3NF: User roles/permissions.
 * Maintains OneToMany relationship with User.
 */
@Entity
@Table(name = "role", schema = "identity")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "users")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<User> users = new ArrayList<>();
}
