package com.iamfit.autenticacion_seguridad.repository;

import com.iamfit.autenticacion_seguridad.entity.PrivilegeEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrivilegeRepository extends JpaRepository<PrivilegeEntity, UUID> {

    /**
     * Obtener privilegio por nombre.
     * @param name Nombre del privilegio.
     * @return Privilegio obtenido.
     */
    Optional<PrivilegeEntity> findByName(String name);

    /**
     * Validar si existe cierto privilegio por su nombre.
     * @param name Nombre del privilegio.
     * @return si el privilegio existe.
     */
    boolean existsByName(String name);

    /**
     * Desactivar Privilegio.
     * @param id El id del privilegio.
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
           UPDATE PrivilegeEntity p 
           SET p.isActive = false, 
               p.updatedAt = CAST(current_timestamp AS Instant)
           WHERE p.id = :id
           """)
    void deactivateById(UUID id);
}
