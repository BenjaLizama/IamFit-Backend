package com.iamfit.autenticacion_seguridad.repository;

import com.iamfit.autenticacion_seguridad.entity.PrivilegeEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PrivilegeRepositoryTest {

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Debería encontrar un privilegio por su nombre")
    void findByName_ShouldReturnPrivilege() {
        PrivilegeEntity privilege = new PrivilegeEntity();
        privilege.setName("READ_PRIVILEGE");
        privilege.setDescription("Permiso de lectura");
        privilege.setIsActive(true);

        entityManager.persist(privilege);
        entityManager.flush();

        Optional<PrivilegeEntity> result = privilegeRepository.findByName("READ_PRIVILEGE");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("READ_PRIVILEGE");
    }

    @Test
    @DisplayName("Debería retornar true si el nombre del privilegio existe")
    void existsByName_ShouldReturnTrue() {
        PrivilegeEntity privilege = new PrivilegeEntity();
        privilege.setName("WRITE_PRIVILEGE");
        privilege.setDescription("Permiso de escritura");
        privilege.setIsActive(true);

        entityManager.persist(privilege);
        entityManager.flush();

        boolean exists = privilegeRepository.existsByName("WRITE_PRIVILEGE");
        boolean notExists = privilegeRepository.existsByName("DELETE_PRIVILEGE");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Debería desactivar el privilegio por ID y actualizar updatedAt")
    void deactivateById_ShouldUpdateStatusAndTimestamp() {
        PrivilegeEntity privilege = new PrivilegeEntity();
        privilege.setName("UPDATE_PRIVILEGE");
        privilege.setDescription("Permiso de actualización");
        privilege.setIsActive(true);

        PrivilegeEntity saved = entityManager.persistFlushFind(privilege);
        UUID id = saved.getId();

        privilegeRepository.deactivateById(id);

        entityManager.flush();
        entityManager.clear();

        Optional<PrivilegeEntity> updated = privilegeRepository.findById(id);

        assertThat(updated).isPresent();
        assertThat(updated.get().getIsActive()).isFalse();
        assertThat(updated.get().getUpdatedAt()).isNotNull();
    }
}