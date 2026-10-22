package com.alertamujer.identity.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Row of a registered person. The domain is User (with the role as a field). */
@Entity
@Table(name = "users", schema = "identity")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
class UserJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // EAGER: the role name is read when converting to the domain, outside the JPA session
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_users_role"))
    @ToString.Exclude
    private RoleJpaEntity role;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(unique = true)
    private String telephone;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "document_number", unique = true)
    private String documentNumber;

    @Column(name = "document_type")
    private String documentType;

    private LocalDate birthdate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "terms_accepted_at")
    private LocalDateTime termsAcceptedAt;

    @Column(name = "terms_version")
    private String termsVersion;
}
