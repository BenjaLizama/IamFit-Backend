package com.iamfit.autenticacion_seguridad.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
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
@Table(name = "PRIVILEGE")
public class PrivilegeEntity extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name; // Ej: READ_USERS, WRITE_USERS.

    @Column(nullable = false)
    private String description;

    @ManyToMany(mappedBy = "privileges")
    private Set<RoleEntity> roles = new HashSet<>();
}
