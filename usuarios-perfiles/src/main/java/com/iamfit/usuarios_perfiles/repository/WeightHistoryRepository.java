package com.iamfit.usuarios_perfiles.repository;

import com.iamfit.usuarios_perfiles.entity.WeightHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WeightHistoryRepository extends JpaRepository<WeightHistoryEntity, UUID> {

    @Query("SELECT w FROM WeightHistoryEntity w WHERE w.user.credentialId = :credentialId ORDER BY w.createdAt ASC")
    List<WeightHistoryEntity> findByCredentialIdOrderByCreatedAt(@Param("credentialId") UUID credentialId);
}