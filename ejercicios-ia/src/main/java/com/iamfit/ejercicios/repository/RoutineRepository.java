package com.iamfit.ejercicios.repository;

import com.iamfit.ejercicios.entity.Routine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoutineRepository extends JpaRepository<Routine, UUID> {

    List<Routine> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(String userId);

    long countByUserIdAndIsActiveTrue(String userId);

    Optional<Routine> findByIdAndUserId(UUID id, String userId);

    List<Routine> findByUserIdAndIsActiveFalseOrderByCreatedAtDesc(String userId);
    List<Routine> findByUserIdOrderByCreatedAtDesc(String userId);
    long countByUserIdAndIsActiveFalse(String userId);
}