package com.iamfit.autenticacion_seguridad.security;

import com.iamfit.autenticacion_seguridad.entity.CredentialEntity;
import com.iamfit.autenticacion_seguridad.entity.PrivilegeEntity;
import com.iamfit.autenticacion_seguridad.entity.RoleEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SecurityCredentialTest {

    private CredentialEntity entity;
    private SecurityCredential securityCredential;

    @BeforeEach
    void setUp() {
        // Preparar Privilegios
        PrivilegeEntity readPriv = new PrivilegeEntity();
        readPriv.setName("READ_PRIVILEGE");

        // Preparar Rol
        RoleEntity adminRole = new RoleEntity();
        adminRole.setRoleName("ROLE_ADMIN");
        adminRole.setPrivileges(Set.of(readPriv));

        // Preparar Entidad Credencial
        entity = new CredentialEntity();
        entity.setId(UUID.randomUUID());
        entity.setEmail("admin@test.com");
        entity.setPassword("encodedPassword");
        entity.setIsActive(true);
        entity.setRoleList(Set.of(adminRole));

        securityCredential = new SecurityCredential(entity);
    }

    @Test
    @DisplayName("Debería retornar el ID correcto")
    void getIdTest() {
        assertEquals(entity.getId(), securityCredential.getId());
    }

    @Test
    @DisplayName("Debería retornar el Username (email)")
    void getUsernameTest() {
        assertEquals("admin@test.com", securityCredential.getUsername());
    }

    @Test
    @DisplayName("Debería retornar la contraseña")
    void getPasswordTest() {
        assertEquals("encodedPassword", securityCredential.getPassword());
    }

    @Test
    @DisplayName("Debería aplanar Roles y Privilegios en Authorities")
    void getAuthoritiesTest() {
        Collection<? extends GrantedAuthority> authorities = securityCredential.getAuthorities();

        assertNotNull(authorities);
        assertEquals(2, authorities.size()); // 1 Rol + 1 Privilegio

        boolean hasRole = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean hasPrivilege = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("READ_PRIVILEGE"));

        assertTrue(hasRole);
        assertTrue(hasPrivilege);
    }

    @Test
    @DisplayName("Debería reflejar el estado activo de la cuenta")
    void isEnabledTest() {
        assertTrue(securityCredential.isEnabled());

        entity.setIsActive(false);
        assertFalse(securityCredential.isEnabled());
    }

    @Test
    @DisplayName("Debería retornar true para los estados por defecto de UserDetails")
    void defaultUserDetailsStatesTest() {
        assertTrue(securityCredential.isAccountNonExpired());
        assertTrue(securityCredential.isAccountNonLocked());
        assertTrue(securityCredential.isCredentialsNonExpired());
    }
}