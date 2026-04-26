package com.iamfit.autenticacion_seguridad.security;

import com.iamfit.autenticacion_seguridad.entity.CredentialEntity;
import com.iamfit.autenticacion_seguridad.entity.PrivilegeEntity;
import com.iamfit.autenticacion_seguridad.entity.RoleEntity;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@AllArgsConstructor
public class SecurityCredential implements UserDetails {

    private final CredentialEntity credential;

    public UUID getId() {
        return credential.getId();
    }

    @Override
    @NullMarked
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        credential.getRoleList().forEach(role -> {
            // Añade el Rol (ej: ROLE_USER)
            authorities.add(new SimpleGrantedAuthority(role.getRoleName()));

            // Añade todos los privilegios de ese rol (ej: READ_USERS)
            role.getPrivileges().stream()
                    .map(privilege -> new SimpleGrantedAuthority(privilege.getName()))
                    .forEach(authorities::add);
        });

        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return credential.getPassword();
    }

    @Override
    @NullMarked
    public String getUsername() {
        return credential.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return credential.getIsActive();
    }

    // --- Métodos de ayuda para extraer Roles y Privilegios ---

    private List<String> getPrivileges(Collection<RoleEntity> roles) {
        List<String> privileges = new ArrayList<>();
        List<PrivilegeEntity> collection = new ArrayList<>();

        for (RoleEntity role : roles) {
            privileges.add(role.getRoleName());
            collection.addAll(role.getPrivileges());
        }

        for (PrivilegeEntity item : collection) {
            privileges.add(item.getName());
        }
        return privileges;
    }

    private List<GrantedAuthority> getGrantedAuthorities(List<String> privileges) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String privilege : privileges) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }
        return authorities;
    }

}
