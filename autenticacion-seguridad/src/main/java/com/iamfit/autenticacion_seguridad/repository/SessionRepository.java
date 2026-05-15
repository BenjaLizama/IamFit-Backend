package com.iamfit.autenticacion_seguridad.repository;

import com.iamfit.autenticacion_seguridad.entity.CredentialEntity;
import com.iamfit.autenticacion_seguridad.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, UUID> {

    Optional<SessionEntity> findByRefreshTokenHash(String refreshTokenHash);

    // Encuentra una sesión específica de un usuario en un dispositivo concreto
    Optional<SessionEntity> findByCredentialAndDeviceId(CredentialEntity credential, String deviceId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE from SessionEntity s WHERE s.credential = :credential")
    void deleteByCredential(CredentialEntity credential);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
           UPDATE SessionEntity s 
           SET s.isActive = false, 
               s.updatedAt = CAST(current_timestamp AS Instant) 
           WHERE s.credential.id = :credentialId 
           AND s.isActive = true
           """)
    void revokeAllByCredentialId(UUID credentialId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM SessionEntity s WHERE s.expiryDate < :now")
    int deleteByExpiryDateBefore(Instant now);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
       UPDATE SessionEntity s 
       SET s.isActive = false, 
           s.updatedAt = CAST(current_timestamp AS Instant) 
       WHERE s.credential.id = :credentialId 
       AND s.deviceId = :deviceId
       AND s.isActive = true
       """)
    void revokeByCredentialIdAndDeviceId(UUID credentialId, String deviceId);

}