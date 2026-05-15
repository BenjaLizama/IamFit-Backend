package com.iamfit.usuarios_perfiles.repository;

import com.iamfit.usuarios_perfiles.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * Busca un perfil en base al ID de la credencial (que viaja en el token).
     * @param credentialId UUID que viaja en el token de acceso.
     * @return Usuario del sistema.
     */
    Optional<UserEntity> findByCredentialId(UUID credentialId);

    /**
     * Verifica si existe un perfil para una credencial; específica.
     * @param credentialId UUID que viaja en el token de acceso.
     * @return Booleano de sí existe el usuario.
     */
    boolean existsByCredentialId(UUID credentialId);

}
