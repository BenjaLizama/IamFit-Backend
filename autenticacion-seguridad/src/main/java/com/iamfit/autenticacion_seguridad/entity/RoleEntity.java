package com.iamfit.autenticacion_seguridad.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Table(name = "ROLE")
public class RoleEntity extends BaseEntity {

    @Column(nullable = false)
    private String roleName;

    @Column(nullable = false)
    private String roleDescription;

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinTable(
            name = "role_privileges",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "privilege_id")
    )
    private Set<PrivilegeEntity> privileges = new HashSet<>();

}
