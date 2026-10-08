package com.alertamujer.identity.domain.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * User entity following database schema: Contains personal data and role.
 * Credentials are in Account, biometrics/preferences in UserProfile.
 */
@Entity
@Table(name = "users", schema = "identity")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "role")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_users_role"))
    @ToString.Exclude
    private Role role;

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
    private LocalDateTime createdAt = LocalDateTime.now();
}
