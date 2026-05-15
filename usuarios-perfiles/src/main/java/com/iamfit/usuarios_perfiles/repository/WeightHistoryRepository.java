package com.iamfit.usuarios_perfiles.repository;

import com.iamfit.usuarios_perfiles.entity.WeightHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WeightHistoryRepository extends JpaRepository<WeightHistoryEntity, UUID> {

}
