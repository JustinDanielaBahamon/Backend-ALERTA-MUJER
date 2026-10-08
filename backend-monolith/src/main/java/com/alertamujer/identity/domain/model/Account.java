package com.alertamujer.identity.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Account entity following database schema: Contains authentication credentials.
 * OneToOne with User via user_id foreign key.
 */
@Entity
@Table(name = "account", schema = "identity")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "user")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_account_user"))
    @ToString.Exclude
    private User user;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    private String status = "active";

    @Column(name = "last_access")
    private LocalDateTime lastAccess;
}
