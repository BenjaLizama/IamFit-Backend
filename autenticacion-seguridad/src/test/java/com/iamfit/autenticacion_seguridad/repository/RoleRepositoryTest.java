package com.iamfit.autenticacion_seguridad.repository;

import com.iamfit.autenticacion_seguridad.entity.RoleEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Debería encontrar un rol por su nombre")
    void findByRoleName_ShouldReturnRole() {
        RoleEntity role = new RoleEntity();
        role.setRoleName("ROLE_ADMIN");
        role.setRoleDescription("Administrador del sistema");
        role.setIsActive(true);

        entityManager.persist(role);
        entityManager.flush();

        Optional<RoleEntity> result = roleRepository.findByRoleName("ROLE_ADMIN");

        assertThat(result).isPresent();
        assertThat(result.get().getRoleName()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Debería retornar true si el nombre del rol existe")
    void existsByRoleName_ShouldReturnTrue() {
        RoleEntity role = new RoleEntity();
        role.setRoleName("ROLE_USER");
        role.setRoleDescription("Usuario estándar");
        role.setIsActive(true);

        entityManager.persist(role);
        entityManager.flush();

        boolean exists = roleRepository.existsByRoleName("ROLE_USER");
        boolean notExists = roleRepository.existsByRoleName("ROLE_GUEST");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Debería desactivar el rol por ID y actualizar campos de auditoría")
    void deactivateById_ShouldUpdateStatusAndTimestamp() {
        RoleEntity role = new RoleEntity();
        role.setRoleName("ROLE_TO_DEACTIVATE");
        role.setRoleDescription("Rol temporal");
        role.setIsActive(true);

        RoleEntity saved = entityManager.persistFlushFind(role);
        UUID id = saved.getId();

        roleRepository.deactivateById(id);

        entityManager.flush();
        entityManager.clear();

        Optional<RoleEntity> updated = roleRepository.findById(id);

        assertThat(updated).isPresent();
        assertThat(updated.get().getIsActive()).isFalse();
        assertThat(updated.get().getUpdatedAt()).isNotNull();
    }
}