package com.iamfit.autenticacion_seguridad.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "CREDENTIAL", indexes = {
        @Index(name = "ux_credential_email", columnList = "email", unique = true)
})
@Getter @Setter
public class CredentialEntity extends BaseEntity {

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonManagedReference
    @JoinTable(
            name = "credential_role",
            joinColumns = @JoinColumn(name = "credential_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roleList;

    @Column(name = "deactivated_at")
    private Instant deactivatedAt;

    @Version
    private Long version; // el número de la versión del registro

}
