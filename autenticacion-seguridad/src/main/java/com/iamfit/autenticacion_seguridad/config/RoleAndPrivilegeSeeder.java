package com.iamfit.autenticacion_seguridad.config;

import com.iamfit.autenticacion_seguridad.entity.PrivilegeEntity;
import com.iamfit.autenticacion_seguridad.entity.RoleEntity;
import com.iamfit.autenticacion_seguridad.repository.PrivilegeRepository;
import com.iamfit.autenticacion_seguridad.repository.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleAndPrivilegeSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;

    private final Map<String, PrivilegeEntity> privilegeCache = new HashMap<>();

    @Override
    @Transactional
    public void run(String @NonNull ... args) {
        log.info("--- Iniciando Seeder de Seguridad ---");

        // Crear privilegios SIEMPRE (porque el método ya evita duplicados)
        PrivilegeEntity read = createPrivilegeIfNotFound("READ_PRIVILEGE", "Leer datos.");
        PrivilegeEntity write = createPrivilegeIfNotFound("WRITE_PRIVILEGE", "Crear/editar datos.");
        PrivilegeEntity delete = createPrivilegeIfNotFound("DELETE_PRIVILEGE", "Eliminar datos.");

        createRoleIfNotExists("ROLE_ADMIN", "Administrador del sistema.", Set.of(read, write, delete));
        createRoleIfNotExists("ROLE_USER", "Usuario común del sistema.", Set.of(read));

        log.info("--- Seeder de Seguridad finalizado ---");
    }

    private PrivilegeEntity createPrivilegeIfNotFound(String name, String description) {

        if (privilegeCache.containsKey(name)) {
            return privilegeCache.get(name);
        }

        PrivilegeEntity privilege = privilegeRepository.findByName(name)
                .orElseGet(() -> {
                    PrivilegeEntity newPrivilege = new PrivilegeEntity();
                    newPrivilege.setName(name);
                    newPrivilege.setDescription(description);
                    newPrivilege.setIsActive(true);
                    return privilegeRepository.saveAndFlush(newPrivilege);
                });
        privilegeCache.put(name, privilege);
        return privilege;
    }

    private void createRoleIfNotExists(String name, String desc, Set<PrivilegeEntity> privileges) {
        if (roleRepository.existsByRoleName(name)) {
            log.info("Role {} ya existe", name);
            return;
        }

        RoleEntity role = new RoleEntity();
        role.setRoleName(name);
        role.setRoleDescription(desc);
        role.setPrivileges(new HashSet<>(privileges));
        role.setIsActive(true);

        roleRepository.saveAndFlush(role);

        log.info("Role {} creado", name);
    }
}