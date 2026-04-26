package com.iamfit.autenticacion_seguridad.repository;

import com.iamfit.autenticacion_seguridad.entity.RoleEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    /**
     * Obtener rol por su nombre.
     */
    Optional<RoleEntity> findByRoleName(String roleName);

    /**
     * Validar existencia de un rol.
     */
    boolean existsByRoleName(String roleName);

    /**
     * Desactivación de Rol con trazabilidad inyectada.
     * Al igual que en Credential y Privilege, aseguramos que el updatedAt
     * se actualice en la base de datos a pesar de ser una query @Modifying.
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
           UPDATE RoleEntity r 
           SET r.isActive = false, 
               r.updatedAt = CAST(current_timestamp AS Instant)
           WHERE r.id = :id
           """)
    void deactivateById(UUID id);
}