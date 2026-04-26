package com.iamfit.autenticacion_seguridad.config;

import com.iamfit.autenticacion_seguridad.entity.PrivilegeEntity;
import com.iamfit.autenticacion_seguridad.entity.RoleEntity;
import com.iamfit.autenticacion_seguridad.repository.PrivilegeRepository;
import com.iamfit.autenticacion_seguridad.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleAndPrivilegeSeederTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PrivilegeRepository privilegeRepository;

    @InjectMocks
    private RoleAndPrivilegeSeeder seeder;

    private PrivilegeEntity createPriv(String name) {
        PrivilegeEntity p = new PrivilegeEntity();
        p.setName(name);
        return p;
    }

    @Test
    @DisplayName("Debería crear privilegios y roles cuando la base de datos está vacía")
    void shouldCreateEverythingWhenDbIsEmpty() {
        when(privilegeRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(privilegeRepository.saveAndFlush(any(PrivilegeEntity.class)))
                .thenAnswer(i -> i.getArguments()[0]);
        when(roleRepository.existsByRoleName(anyString())).thenReturn(false);

        seeder.run();

        verify(privilegeRepository, times(3)).saveAndFlush(any(PrivilegeEntity.class));
        verify(roleRepository, times(2)).saveAndFlush(any(RoleEntity.class));
    }

    @Test
    @DisplayName("No debería crear nada si ya existen los roles y privilegios")
    void shouldNotCreateWhenAlreadyExists() {
        when(privilegeRepository.findByName("READ_PRIVILEGE")).thenReturn(Optional.of(createPriv("READ")));
        when(privilegeRepository.findByName("WRITE_PRIVILEGE")).thenReturn(Optional.of(createPriv("WRITE")));
        when(privilegeRepository.findByName("DELETE_PRIVILEGE")).thenReturn(Optional.of(createPriv("DELETE")));

        when(roleRepository.existsByRoleName(anyString())).thenReturn(true);

        seeder.run();

        verify(privilegeRepository, never()).saveAndFlush(any());
        verify(roleRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Debería usar el caché de privilegios para evitar consultas repetidas")
    void shouldUseCacheForPrivileges() {
        when(privilegeRepository.findByName("READ_PRIVILEGE")).thenReturn(Optional.of(createPriv("READ")));
        when(privilegeRepository.findByName("WRITE_PRIVILEGE")).thenReturn(Optional.of(createPriv("WRITE")));
        when(privilegeRepository.findByName("DELETE_PRIVILEGE")).thenReturn(Optional.of(createPriv("DELETE")));

        when(roleRepository.existsByRoleName(anyString())).thenReturn(true);

        seeder.run();

        verify(privilegeRepository, times(3)).findByName(anyString());
    }
}
