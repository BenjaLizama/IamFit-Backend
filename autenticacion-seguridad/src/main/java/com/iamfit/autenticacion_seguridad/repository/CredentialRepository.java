package com.iamfit.autenticacion_seguridad.repository;

import com.iamfit.autenticacion_seguridad.entity.CredentialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CredentialRepository extends JpaRepository<CredentialEntity, UUID> {

    /**
     * Encontrar credencial en base al correo proporcionado.
     * @param email Correo buscado en la BD.
     * @return La credenecial encontrada.
     */
    @Query("SELECT c FROM CredentialEntity c " +
            "LEFT JOIN FETCH c.roleList r " +
            "LEFT JOIN FETCH r.privileges " +
            "WHERE c.email = :email")
    Optional<CredentialEntity> findByEmail(String email);

    /**
     * Validar si una credencial existe en base al correo proporcionado.
     * @param email Correo buscado en la base de datos.
     * @return Si el correo existe.
     */
    boolean existsByEmail(String email);

    /**
     * Limpieza masiva de cuentas inactivas.
     * Usamos JPQL directo para que sea una sola sentencia SQL
     * clearAutomatically = true | asegura que si teníamos estas entidades en cache, se eliminen.
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM CredentialEntity c WHERE c.isActive = false AND c.deactivatedAt < :cutoff")
    int deleteOldInactiveAccounts(Instant cutoff);

    /**
     * Desactivación con trazabilidad temporal inyectada.
     * Soluciona el problema de @LastModifiedDate en actualizaciones masivas.
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
           UPDATE CredentialEntity c 
           SET c.isActive = false, 
               c.deactivatedAt = CAST(current_timestamp AS Instant), 
               c.updatedAt = CAST(current_timestamp AS Instant) 
           WHERE c.id = :id
           """)
    void deactivateById(UUID id);

}